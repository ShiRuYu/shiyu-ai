package com.shiyu.ai.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiyu.ai.runtimeconsole.auth.ConsoleSessionStore;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.education.implementation.application.ExamService;
import com.shiyu.ai.education.implementation.application.ExamPublishingService;
import com.shiyu.ai.education.implementation.application.ExamTakingService;
import com.shiyu.ai.education.implementation.application.CourseService;
import com.shiyu.ai.education.implementation.application.StudentLearningService;
import com.shiyu.ai.education.implementation.application.TrustedScoreAnalysisService;
import com.shiyu.ai.education.implementation.web.request.CourseRequest;
import com.shiyu.ai.education.implementation.web.request.ExamRequest;
import com.shiyu.ai.education.implementation.web.request.ExamCompositionRequest;
import com.shiyu.ai.education.implementation.web.request.SubmitExamRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.implementation.infrastructure.service.ModelManager;
import com.shiyu.ai.model.implementation.persistence.repository.PlatformCredentialCipher;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.URI;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 验证 Core Journey Integration 相关功能、边界条件、异常路径和协作行为。
 */
@SpringBootTest(
        classes = ShiyuBootstrapApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoreJourneyIntegrationTest {

    private static final String PREVIOUS_APP_HOME = System.getProperty("app.home");
    private static final Path APP_HOME =
            Path.of(
                    System.getProperty("java.io.tmpdir"),
                    "shiyu-core-journey-" + UUID.randomUUID());

    static {
        System.setProperty("app.home", APP_HOME.toString());
    }

    @AfterAll
    static void restoreAppHome() {
        if (PREVIOUS_APP_HOME == null) {
            System.clearProperty("app.home");
        } else {
            System.setProperty("app.home", PREVIOUS_APP_HOME);
        }
    }

    @DynamicPropertySource
    static void appHome(DynamicPropertyRegistry registry) {
        registry.add("app.home", () -> APP_HOME.toString());
        registry.add("spring.profiles.active", () -> "dev");
        registry.add("shiyu.modules.education.enabled", () -> "true");
        registry.add("shiyu.console.announce-startup-link", () -> "false");
        registry.add("shiyu.ai.platform-key-encryption-key", () ->
                java.util.Base64.getEncoder().encodeToString(
                        "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
    }

    @Autowired @LocalServerPort private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private AgentExecutionRepository agentExecutionRepository;

    @Autowired private ModelManager modelManager;

    @Autowired private PlatformCredentialCipher platformCredentialCipher;

    @Test
    void concurrentResumeClaimsAreSerializedByTheDatabase() throws Exception {
        TenantId tenantId = new TenantId(1L);
        String executionId = "resume-claim-" + UUID.randomUUID();
        String checkpointId = "checkpoint-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO AGENT_EXECUTION"
                        + " (EXECUTION_ID, AGENT_ID, VERSION, USER_ID, TENANT_ID,"
                        + " LAST_CHECKPOINT_ID, STATUS, START_TIME)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                executionId, "resume-test-agent", "v1", 2L, tenantId.value(),
                checkpointId, 3);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        var callers = Executors.newFixedThreadPool(2);
        try {
            var first = callers.submit(() -> {
                ready.countDown();
                start.await();
                return TenantScope.withTenant(tenantId, () ->
                        agentExecutionRepository.claimPausedForResume(
                                tenantId, executionId, checkpointId));
            });
            var second = callers.submit(() -> {
                ready.countDown();
                start.await();
                return TenantScope.withTenant(tenantId, () ->
                        agentExecutionRepository.claimPausedForResume(
                                tenantId, executionId, checkpointId));
            });
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            int claims = (first.get(5, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(5, TimeUnit.SECONDS) ? 1 : 0);
            assertThat(claims).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT STATUS FROM AGENT_EXECUTION WHERE EXECUTION_ID=? AND TENANT_ID=?",
                    Integer.class, executionId, tenantId.value())).isZero();
        } finally {
            callers.shutdownNow();
        }
    }

    @Test
    void platformApiCanExplicitlyClearAStoredCredentialWithoutReturningIt() throws Exception {
        String code = "CLEAR_KEY_" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 10).toUpperCase(java.util.Locale.ROOT);
        String key = "temporary-integration-key";
        JsonNode platformLogin = body(request("POST", "/api/iam/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"123456\"}").body());
        assertThat(platformLogin.path("success").asBoolean())
                .as("platform test login failed: code=%s, message=%s",
                        platformLogin.path("code").asText(), platformLogin.path("message").asText())
                .isTrue();
        String authorization = "Bearer " + platformLogin.path("data").path("accessToken").asText();
        long platformId = -1L;
        try {
            JsonNode created = body(request("POST", "/api/model/platforms", authorization,
                    objectMapper.writeValueAsString(Map.of(
                            "name", "Credential clear test", "code", code,
                            "adapterType", "OPENAI_COMPATIBLE",
                            "baseUrl", "http://127.0.0.1:1/v1",
                            "apiKey", key, "status", "1"))).body());
            assertThat(created.path("success").asBoolean())
                    .as("platform create failed: code=%s, message=%s",
                            created.path("code").asText(), created.path("message").asText())
                    .isTrue();
            platformId = created.path("data").path("id").asLong();
            assertThat(platformId).isPositive();
            assertThat(created.toString()).doesNotContain(key);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT api_key FROM model_ai_platform WHERE id = ?", String.class,
                    platformId)).startsWith("enc:v1:").doesNotContain(key);

            JsonNode cleared = body(request("PUT", "/api/model/platforms/" + platformId,
                    authorization, objectMapper.writeValueAsString(Map.of(
                            "name", "Credential clear test", "code", code,
                            "adapterType", "OPENAI_COMPATIBLE",
                            "baseUrl", "http://127.0.0.1:1/v1",
                            "clearApiKey", true, "status", "1"))).body());
            assertSuccess(cleared);
            assertThat(cleared.toString()).doesNotContain(key);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT api_key FROM model_ai_platform WHERE id = ?", String.class,
                    platformId)).isEmpty();
        } finally {
            if (platformId > 0) {
                jdbcTemplate.update("DELETE FROM model_ai_platform WHERE id = ?", platformId);
                modelManager.markDirty();
            }
        }
    }

    @Test
    void authenticatedTenantsReachOnlyTheirOwnModelEndpoints() throws Exception {
        AtomicInteger firstCalls = new AtomicInteger();
        AtomicInteger secondCalls = new AtomicInteger();
        HttpServer first = tenantModelEndpoint("first", "first-test-key", firstCalls);
        HttpServer second = tenantModelEndpoint("second", "second-test-key", secondCalls);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String code = "TENANT_HTTP_" + suffix.toUpperCase(java.util.Locale.ROOT);
        String username = "model_tenant_" + suffix;
        long secondTenant = -1L;
        long secondUser = -1L;
        long secondRole = -1L;
        try {
            jdbcTemplate.update("INSERT INTO auth_tenant(code,name,status,del_flag) VALUES (?,?,1,0)",
                    "model-" + suffix, "Model tenant");
            secondTenant = jdbcTemplate.queryForObject(
                    "SELECT id FROM auth_tenant WHERE code = ?", Long.class, "model-" + suffix);
            String passwordHash = jdbcTemplate.queryForObject(
                    "SELECT password FROM auth_user WHERE username = 'admin'", String.class);
            jdbcTemplate.update("INSERT INTO auth_user(username,password,status,del_flag,ext_info) "
                            + "VALUES (?,?,1,0,?)", username, passwordHash,
                    "{\"homeTenantId\":" + secondTenant + ",\"currentTenantId\":"
                            + secondTenant + "}");
            secondUser = jdbcTemplate.queryForObject(
                    "SELECT id FROM auth_user WHERE username = ?", Long.class, username);
            jdbcTemplate.update("INSERT INTO auth_role(code,name,tenant_id,status,del_flag) "
                    + "VALUES ('user','Model user',?,1,0)", secondTenant);
            secondRole = jdbcTemplate.queryForObject(
                    "SELECT id FROM auth_role WHERE tenant_id = ? AND name = 'Model user'",
                    Long.class, secondTenant);
            jdbcTemplate.update("INSERT INTO auth_user_scope_role(user_id,role_id,tenant_id,status,del_flag) "
                    + "VALUES (?,?,?,1,0)", secondUser, secondRole, secondTenant);

            jdbcTemplate.update("INSERT INTO model_ai_platform(name,code,adapter_type,tenant_id,base_url,api_key,status,del_flag) "
                            + "VALUES (?,?, 'OPENAI_COMPATIBLE',1,?,?,1,0)",
                    "First model endpoint", code, modelUrl(first),
                    platformCredentialCipher.protect(1L, "first-test-key"));
            jdbcTemplate.update("INSERT INTO model_ai_platform(name,code,adapter_type,tenant_id,base_url,api_key,status,del_flag) "
                            + "VALUES (?,?, 'OPENAI_COMPATIBLE',?,?,?,1,0)",
                    "Second model endpoint", code, secondTenant, modelUrl(second),
                    platformCredentialCipher.protect(secondTenant, "second-test-key"));
            long firstPlatform = jdbcTemplate.queryForObject(
                    "SELECT id FROM model_ai_platform WHERE code = ? AND tenant_id = 1",
                    Long.class, code);
            long secondPlatform = jdbcTemplate.queryForObject(
                    "SELECT id FROM model_ai_platform WHERE code = ? AND tenant_id = ?",
                    Long.class, code, secondTenant);
            jdbcTemplate.update("INSERT INTO model_ai_model(platform_id,model_name,tenant_id,is_default,status,del_flag) "
                    + "VALUES (?,'first-model',1,'Y',1,0)", firstPlatform);
            jdbcTemplate.update("INSERT INTO model_ai_model(platform_id,model_name,tenant_id,is_default,status,del_flag) "
                    + "VALUES (?,'second-model',?,'Y',1,0)", secondPlatform, secondTenant);
            modelManager.markDirty();

            String firstToken = body(request("POST", "/api/iam/auth/login", null,
                    "{\"username\":\"admin\",\"password\":\"123456\"}").body())
                    .path("data").path("accessToken").asText();
            String secondToken = body(request("POST", "/api/iam/auth/login", null,
                    "{\"username\":\"" + username + "\",\"password\":\"123456\"}").body())
                    .path("data").path("accessToken").asText();
            assertThat(firstToken).isNotBlank();
            assertThat(secondToken).isNotBlank();
            assertTenantCompletion("Bearer " + firstToken, code, "first-model", "first");
            assertTenantCompletion("Bearer " + secondToken, code, "second-model", "second");
            assertTenantStream("Bearer " + firstToken, code, "first-model", "first");
            assertTenantStream("Bearer " + secondToken, code, "second-model", "second");
            assertThat(firstCalls.get()).isEqualTo(2);
            assertThat(secondCalls.get()).isEqualTo(2);
        } finally {
            jdbcTemplate.update("DELETE FROM model_ai_model WHERE platform_id IN "
                    + "(SELECT id FROM model_ai_platform WHERE code = ?)", code);
            jdbcTemplate.update("DELETE FROM model_ai_platform WHERE code = ?", code);
            if (secondUser > 0) {
                jdbcTemplate.update("DELETE FROM auth_user_scope_role WHERE user_id = ?", secondUser);
                jdbcTemplate.update("DELETE FROM auth_user WHERE id = ?", secondUser);
            }
            if (secondRole > 0) jdbcTemplate.update("DELETE FROM auth_role WHERE id = ?", secondRole);
            if (secondTenant > 0) jdbcTemplate.update("DELETE FROM auth_tenant WHERE id = ?", secondTenant);
            modelManager.markDirty();
            first.stop(0);
            second.stop(0);
        }
    }

    private void assertTenantCompletion(String authorization, String code, String model,
                                        String expected) throws Exception {
        HttpResponse<String> response = request("POST", "/api/conversation/chat/completions",
                authorization, objectMapper.writeValueAsString(Map.of(
                        "platform", code, "model", model,
                        "messages", List.of(Map.of("role", "user", "content", "hello")))));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(expected);
    }

    private void assertTenantStream(String authorization, String code, String model,
                                    String expected) throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
                "platform", code, "model", model, "stream", true,
                "messages", List.of(Map.of("role", "user", "content", "hello"))));
        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/conversation/chat/completions"))
                .header("Authorization", authorization)
                .header("Accept", "text/event-stream")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(expected, "[DONE]");
    }

    private static String modelUrl(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
    }

    private static HttpServer tenantModelEndpoint(String name, String key, AtomicInteger calls)
            throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            if (!("Bearer " + key).equals(authorization)) {
                exchange.sendResponseHeaders(401, -1);
                exchange.close();
                return;
            }
            calls.incrementAndGet();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            boolean stream = body.matches("(?s).*\\\"stream\\\"\\s*:\\s*true.*");
            String response = stream
                    ? "data: {\"id\":\"req\",\"model\":\"" + name
                            + "-model\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\""
                            + name + "\"},\"finish_reason\":null}]}\n\ndata: [DONE]\n\n"
                    : "{\"id\":\"req\",\"model\":\"" + name
                            + "-model\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\""
                            + name + "\"},\"finish_reason\":\"stop\"}]}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type",
                    stream ? "text/event-stream" : "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server;
    }

    @Test
    void loginRejectsOnlyRevokedAndCrossTenantHistoricalRoles() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String username = "stale_scope_" + suffix;
        String staleCode = "stale_" + suffix;
        String foreignCode = "foreign_" + suffix;
        String passwordHash = jdbcTemplate.queryForObject(
                "SELECT password FROM auth_user WHERE username = 'admin'", String.class);
        jdbcTemplate.update("INSERT INTO auth_user(username,password,status,del_flag,ext_info) "
                        + "VALUES (?,?,1,0,?)", username, passwordHash,
                "{\"currentTenantId\":1}");
        long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM auth_user WHERE username = ?", Long.class, username);
        jdbcTemplate.update("INSERT INTO auth_role(code,name,tenant_id,status,del_flag) "
                + "VALUES (?,?,1,0,0)", staleCode, "Revoked");
        jdbcTemplate.update("INSERT INTO auth_role(code,name,tenant_id,status,del_flag) "
                + "VALUES (?,?,2,1,0)", foreignCode, "Foreign");
        long staleId = jdbcTemplate.queryForObject(
                "SELECT id FROM auth_role WHERE code = ?", Long.class, staleCode);
        long foreignId = jdbcTemplate.queryForObject(
                "SELECT id FROM auth_role WHERE code = ?", Long.class, foreignCode);
        try {
            jdbcTemplate.update("INSERT INTO auth_user_scope_role(user_id,role_id,tenant_id,status,del_flag) "
                    + "VALUES (?,?,1,1,0)", userId, staleId);
            jdbcTemplate.update("INSERT INTO auth_user_scope_role(user_id,role_id,tenant_id,status,del_flag) "
                    + "VALUES (?,?,1,1,0)", userId, foreignId);
            JsonNode login = body(request("POST", "/api/iam/auth/login", null,
                    "{\"username\":\"" + username + "\",\"password\":\"123456\"}").body());
            assertThat(login.path("success").asBoolean()).isFalse();
            assertThat(login.path("data").path("accessToken").asText()).isEmpty();
        } finally {
            jdbcTemplate.update("DELETE FROM auth_user_scope_role WHERE user_id = ?", userId);
            jdbcTemplate.update("DELETE FROM auth_role WHERE id IN (?,?)", staleId, foreignId);
            jdbcTemplate.update("DELETE FROM auth_user WHERE id = ?", userId);
        }
    }

    @Autowired private ExamService examService;

    @Autowired private ExamPublishingService examPublishing;

    @Autowired private ExamTakingService examTaking;

    @Autowired private CourseService courseService;

    @Autowired private StudentLearningService studentLearning;

    @Autowired private TrustedScoreAnalysisService scoreAnalysis;
    @MockitoBean private AgentRuntime agentRuntime;

    @Test
    void agentConversationStreamsAndRestoresPersistedMessages() throws Exception {
        HttpResponse<String> login = request("POST", "/api/iam/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"123456\"}");
        String authorization = "Bearer " + body(login.body()).path("data")
                .path("accessToken").asText();
        String appId = body(request("POST", "/api/agent/apps", authorization,
                "{\"name\":\"agent-conversation-test\"}").body())
                .path("data").path("id").asText();
        String versionId = body(request("POST", "/api/agent/apps/" + appId + "/versions",
                authorization,
                objectMapper.writeValueAsString(Map.of("version", "1.0.0",
                        "configJson", objectMapper.writeValueAsString(Map.of(
                                "agentId", "simple-assistant",
                                "validation", Map.of("graph", "PASS"))))))
                .body()).path("data").path("id").asText();
        assertThat(body(request("POST", "/api/agent/apps/" + appId + "/versions/"
                + versionId + "/publish", authorization, null).body())
                .path("success").asBoolean()).isTrue();
        String conversationId = body(request("POST", "/api/conversation/conversations",
                authorization, "{\"sceneType\":\"agent\",\"title\":\"Agent test\"}")
                .body()).path("data").path("id").asText();
        org.mockito.Mockito.when(agentRuntime.executeStream(
                org.mockito.ArgumentMatchers.any(ActorContext.class),
                org.mockito.ArgumentMatchers.eq("simple-assistant"),
                org.mockito.ArgumentMatchers.nullable(String.class),
                org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(reactor.core.publisher.Flux.just(
                        Map.of("status", "STARTED", "executionId", "exec-test",
                                "runtimeRunId", "run-test"),
                        Map.of("status", "COMPLETED", "executionId", "exec-test",
                                "output", Map.of("answer", "ok"))));

        HttpResponse<String> stream = httpClient.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/conversation/conversations/"
                        + conversationId + "/agent-executions"))
                .header("Authorization", authorization)
                .header("Accept", "text/event-stream")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"appId\":\"" + appId
                        + "\",\"prompt\":\"hello\"}"))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(stream.statusCode()).isEqualTo(200);
        assertThat(stream.body()).contains("STARTED", "COMPLETED", "inputMessageId",
                "assistantMessageId", "run-test");
        JsonNode messages = body(request("GET", "/api/conversation/conversations/"
                + conversationId + "/messages", authorization, null).body()).path("data");
        assertThat(messages.size()).isEqualTo(2);
        assertThat(messages.toString()).contains("hello", "answer", "ok", appId);
    }

    @Test
    void studentCatalogFiltersBeforePagingAndPracticeRevealsAnswerOnlyAfterCheck() {
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        TenantScope.withTenant(actor.tenantId(), () -> {
            String subject = "CATALOG" + UUID.randomUUID().toString().substring(0, 8);
            for (int index = 0; index < 3; index++) {
                CourseRequest request = new CourseRequest();
                request.setName("分页课程 " + index);
                request.setSubjectCode(subject);
                request.setGrade(7);
                request.setStatus(1);
                courseService.create(actor, request);
            }
            var second = studentLearning.courses(actor, subject, 7, 2, 2);
            assertThat(second.getTotal()).isEqualTo(3);
            assertThat(second.getItems()).hasSize(1);
            assertThat(studentLearning.courses(actor, subject, 7, 3, 2).getItems()).isEmpty();
            assertThat(studentLearning.courses(actor, subject, 8, 1, 2).getTotal()).isZero();
            assertThatThrownBy(() -> studentLearning.check(actor, 1L, "B"))
                    .isInstanceOf(com.shiyu.ai.common.foundation.exception.ServiceException.class);
            var practice = studentLearning.question(actor, 2L);
            assertThat(practice.toString()).doesNotContain("answer", "移项得");
            assertThat(studentLearning.check(actor, 2L, "B").correct()).isTrue();
            return null;
        });
    }

    @Test
    void practiceSubmissionPersistsOnceAndResolvesWrongQuestionAfterTwoCorrectRetries() {
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        TenantScope.withTenant(actor.tenantId(), () -> {
            String firstKey = UUID.randomUUID().toString();
            var first = studentLearning.submit(actor, 2L, "A", firstKey);
            var retry = studentLearning.submit(actor, 2L, "A", firstKey);
            assertThat(retry.recordId()).isEqualTo(first.recordId());
            assertThat(first.correct()).isFalse();
            assertThat(scoreAnalysis.analyze(actor, first.recordId()).applied()).isTrue();
            assertThat(scoreAnalysis.analyze(actor, first.recordId()).applied()).isFalse();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT ANALYSIS_APPLIED FROM EDU_STUDY_RECORD WHERE ID=?",
                    Boolean.class, first.recordId())).isTrue();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM EDU_STUDY_RECORD WHERE TENANT_ID=1 AND STUDENT_ID=1 AND SUBMISSION_KEY=?",
                    Integer.class, firstKey)).isEqualTo(1);
            assertThat(studentLearning.wrongQuestions(actor).stream()
                    .map(item -> item.questionId())).contains(2L);
            studentLearning.submit(actor, 2L, "B", UUID.randomUUID().toString());
            studentLearning.submit(actor, 2L, "B", UUID.randomUUID().toString());
            assertThat(studentLearning.wrongQuestions(actor).stream()
                    .map(item -> item.questionId())).doesNotContain(2L);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM EDU_STUDY_RECORD WHERE TENANT_ID=1 AND STUDENT_ID=1 AND QUESTION_ID=2 AND SUBMISSION_KEY IS NOT NULL",
                    Integer.class)).isEqualTo(3);
            return null;
        });
    }

    @Test
    void studentExamPersistsAnAnswerFreePaperAndAnOwnedScoredResult() {
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        TenantScope.withTenant(actor.tenantId(), () -> {
        ExamRequest request = new ExamRequest();
        request.setName("集成测验 " + UUID.randomUUID());
        request.setType("UNIT_TEST");
        request.setSubjectCode("MATH");
        request.setGrade(7);
        request.setDurationMin(20);
        request.setTotalScore(100);
        request.setStartTime(LocalDateTime.now().minusMinutes(1));
        request.setEndTime(LocalDateTime.now().plusMinutes(30));
        Long examId = examService.create(actor, request).id();
        examPublishing.replaceComposition(actor, examId, new ExamCompositionRequest(
                List.of(new ExamCompositionRequest.Section("选择题", List.of(
                        new ExamCompositionRequest.Question(1L, BigDecimal.valueOf(100)))))));
        examPublishing.publish(actor, examId);

        var paper = examTaking.start(actor, examId);
        assertThat(paper.attemptId()).isNotNull();
        assertThat(paper.sections().getFirst().questions().getFirst().title()).isNotBlank();
        assertThat(paper.toString()).doesNotContain("移项得", "correctAnswer", "analysis");
        var resumed = examTaking.start(actor, examId);
        assertThat(resumed.attemptId()).isEqualTo(paper.attemptId());

        var scored = examTaking.submit(actor, paper.attemptId(), new SubmitExamRequest(
                List.of(new SubmitExamRequest.Answer(1L, "B"))));
        assertThat(scored.score()).isEqualByComparingTo("100");
        assertThat(examTaking.result(actor, paper.attemptId()).score())
                .isEqualByComparingTo("100");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM EDU_EXAM_ATTEMPT_QUESTION WHERE ATTEMPT_ID=? AND ANSWER='B'",
                Integer.class, paper.attemptId())).isEqualTo(1);
        ActorContext other = new ActorContext(new TenantId(2L), new UserId(2L), false);
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> examTaking.result(other, paper.attemptId()));
        return null;
        });
    }

    @Test
    void closedPageExamIsSettledByBackgroundWorkerWithoutReadingHistory() throws Exception {
        Long attemptId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(ID), 0) + 1000 FROM EDU_EXAM_ATTEMPT", Long.class);
        jdbcTemplate.update(
                "INSERT INTO EDU_EXAM_ATTEMPT_QUESTION "
                        + "(TENANT_ID, ATTEMPT_ID, EXAM_QUESTION_ID, QUESTION_ID, SECTION_ID, "
                        + "SECTION_NAME, SECTION_ORDER_NO, ORDER_NO, TITLE, TYPE, OPTIONS_JSON, "
                        + "CORRECT_ANSWER, MAX_SCORE, ANSWER) VALUES (1, ?, ?, 1, 1, "
                        + "'选择题', 1, 1, '2 + 2?', 'CHOICE', '[\"A. 3\",\"B. 4\"]', "
                        + "'B', 10, 'B')",
                attemptId, attemptId);
        jdbcTemplate.update(
                "INSERT INTO EDU_EXAM_ATTEMPT "
                        + "(ID, TENANT_ID, EXAM_ID, STUDENT_ID, STATUS, STARTED_AT, "
                        + "DEADLINE_AT, TOTAL_SCORE) VALUES (?, 1, ?, ?, 'STARTED', ?, ?, 10)",
                attemptId, attemptId, attemptId,
                LocalDateTime.now().minusMinutes(2), LocalDateTime.now().minusSeconds(1));

        long timeout = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        String status;
        do {
            status = jdbcTemplate.queryForObject(
                    "SELECT STATUS FROM EDU_EXAM_ATTEMPT WHERE ID=?", String.class, attemptId);
            if ("EXPIRED".equals(status)) break;
            Thread.sleep(200);
        } while (System.nanoTime() < timeout);

        assertThat(status).isEqualTo("EXPIRED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT SCORE FROM EDU_EXAM_ATTEMPT WHERE ID=?", BigDecimal.class, attemptId))
                .isEqualByComparingTo("10");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT AWARDED_SCORE FROM EDU_EXAM_ATTEMPT_QUESTION WHERE ATTEMPT_ID=?",
                BigDecimal.class, attemptId)).isEqualByComparingTo("10");
    }

    @Autowired private ConsoleSessionStore consoleSessions;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void openApiIncludesEveryRegisteredBusinessRouteAndGroup() throws Exception {
        Set<String> expected = new TreeSet<>();
        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            if (handler.getBeanType().getPackageName().startsWith("com.shiyu.ai.")) {
                assertThat(mapping.getMethodsCondition().getMethods())
                        .as("Explicit HTTP methods for %s", handler).isNotEmpty();
                mapping.getPatternValues().stream()
                        .filter(path -> path.startsWith("/api/"))
                        .forEach(path -> mapping.getMethodsCondition().getMethods().forEach(method ->
                                expected.add(method.name() + " " + normalizeRoute(path))));
            }
        });
        assertThat(expected).isNotEmpty();
        Set<String> actual = openApiOperations("/v3/api-docs").stream()
                .filter(route -> route.substring(route.indexOf(' ') + 1).startsWith("/api/"))
                .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(openApiOperations("/v3/api-docs/default"))
                .containsExactlyInAnyOrderElementsOf(expected);
        Map<String, String> groups = Map.of(
                "agent", "com.shiyu.ai.agent.implementation.web",
                "auth", "com.shiyu.ai.iam.implementation.web",
                "conversation", "com.shiyu.ai.conversation.implementation.web.controller",
                "education", "com.shiyu.ai.education.implementation.web.controller",
                "knowledge", "com.shiyu.ai.knowledge.implementation.web.controller",
                "model", "com.shiyu.ai.model.implementation.web",
                "usage", "com.shiyu.ai.governance.implementation.web.controller",
                "plugin", "com.shiyu.ai.tooling.implementation.web",
                "memory", "com.shiyu.ai.memory.implementation.web",
                "common", "com.shiyu.ai.common.storage.web");
        for (var group : groups.entrySet()) {
            Set<String> routes = new TreeSet<>();
            handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
                if (handler.getBeanType().getPackageName().startsWith(group.getValue())
                        || (group.getKey().equals("conversation")
                        && handler.getBeanType().getPackageName()
                                .startsWith("com.shiyu.ai.composition.integration.agent"))) {
                    mapping.getPatternValues().stream()
                            .filter(path -> path.startsWith("/api/"))
                            .forEach(path ->
                            mapping.getMethodsCondition().getMethods().forEach(method ->
                                    routes.add(method.name() + " " + normalizeRoute(path))));
                }
            });
            assertThat(routes).as("Registered routes for %s", group.getKey()).isNotEmpty();
            assertThat(openApiOperations("/v3/api-docs/" + group.getKey()))
                    .as("OpenAPI group %s", group.getKey()).containsExactlyInAnyOrderElementsOf(routes);
        }
        assertThat(actual.stream().filter(route -> route.startsWith("GET /api/governance/platform/usage/")).count())
                .isEqualTo(9);
        assertThat(actual).contains("GET /api/education/education-resources/{fileName}");
    }

    @Test
    void resourceCrudUsesCanonicalRestRoutesWithoutLegacyAliases() {
        Set<String> registered = new TreeSet<>();
        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            if (handler.getBeanType().getPackageName().startsWith("com.shiyu.ai.")) {
                mapping.getPatternValues().forEach(path ->
                        mapping.getMethodsCondition().getMethods().forEach(method ->
                                registered.add(method.name() + " " + normalizeRoute(path))));
            }
        });

        assertThat(registered)
                .contains(
                        "GET /api/education/course",
                        "POST /api/education/course",
                        "GET /api/education/course/{id}",
                        "PUT /api/education/course/{id}",
                        "DELETE /api/education/course/{id}",
                        "GET /api/education/chapter/{id}",
                        "POST /api/education/chapter",
                        "PUT /api/education/chapter/{id}",
                        "DELETE /api/education/chapter/{id}",
                        "GET /api/education/exam",
                        "POST /api/education/exam",
                        "GET /api/education/exam/{id}",
                        "PUT /api/education/exam/{id}",
                        "DELETE /api/education/exam/{id}",
                        "GET /api/education/question",
                        "POST /api/education/question",
                        "GET /api/education/question/{id}",
                        "PUT /api/education/question/{id}",
                        "DELETE /api/education/question/{id}",
                        "GET /api/education/resource",
                        "POST /api/education/resource",
                        "GET /api/education/resource/{id}",
                        "PUT /api/education/resource/{id}",
                        "DELETE /api/education/resource/{id}",
                        "GET /api/education/review",
                        "POST /api/education/review",
                        "GET /api/education/review/{id}",
                        "PUT /api/education/review/{id}",
                        "DELETE /api/education/review/{id}",
                        "GET /api/education/students",
                        "POST /api/education/students",
                        "GET /api/education/students/{id}",
                        "PUT /api/education/students/{id}",
                        "DELETE /api/education/students/{id}",
                        "POST /api/education/study-plan",
                        "GET /api/education/study-plan/{id}",
                        "PUT /api/education/study-plan/{id}",
                        "DELETE /api/education/study-plan/{id}",
                        "GET /api/education/subject",
                        "POST /api/education/subject",
                        "GET /api/education/subject/{id}",
                        "PUT /api/education/subject/{id}",
                        "DELETE /api/education/subject/{id}",
                        "GET /api/education/textbook",
                        "POST /api/education/textbook",
                        "GET /api/education/textbook/{id}",
                        "PUT /api/education/textbook/{id}",
                        "DELETE /api/education/textbook/{id}",
                        "POST /api/education/wrong-question",
                        "GET /api/education/wrong-question/{id}",
                        "PUT /api/education/wrong-question/{id}",
                        "DELETE /api/education/wrong-question/{id}",
                        "GET /api/iam/users",
                        "POST /api/iam/users",
                        "PUT /api/iam/users/{id}",
                        "DELETE /api/iam/users/{id}",
                        "GET /api/iam/roles",
                        "POST /api/iam/roles",
                        "GET /api/iam/roles/{id}",
                        "PUT /api/iam/roles/{id}",
                        "DELETE /api/iam/roles/{id}",
                        "GET /api/iam/menus",
                        "POST /api/iam/menus",
                        "PUT /api/iam/menus/{id}",
                        "DELETE /api/iam/menus/{id}",
                        "GET /api/iam/dicts",
                        "POST /api/iam/dicts",
                        "PUT /api/iam/dicts/{id}",
                        "DELETE /api/iam/dicts/{id}",
                        "GET /api/iam/auth-codes",
                        "POST /api/iam/auth-codes",
                        "PUT /api/iam/auth-codes/{id}",
                        "DELETE /api/iam/auth-codes/{id}",
                        "GET /api/agent/agents",
                        "POST /api/agent/agents",
                        "GET /api/agent/agents/{id}",
                        "PUT /api/agent/agents/{id}",
                        "DELETE /api/agent/agents/{id}",
                        "GET /api/agent/agents/{agentId}/versions",
                        "POST /api/agent/agents/{agentId}/versions",
                        "GET /api/agent/agents/{agentId}/versions/{versionId}",
                        "PUT /api/agent/agents/{agentId}/versions/{versionId}",
                        "DELETE /api/agent/agents/{agentId}/versions/{versionId}",
                        "GET /api/agent/intents",
                        "POST /api/agent/intents",
                        "DELETE /api/agent/intents",
                        "GET /api/agent/intents/{id}",
                        "PUT /api/agent/intents/{id}",
                        "DELETE /api/agent/intents/{id}",
                        "GET /api/agent/intents/options",
                        "GET /api/agent/agents/definitions",
                        "GET /api/agent/agents/definitions/{agentId}",
                        "DELETE /api/agent/agents/definitions/{agentId}",
                        "GET /api/iam/tenants",
                        "POST /api/iam/tenants",
                        "GET /api/iam/tenants/tree",
                        "GET /api/iam/tenants/{id}",
                        "PUT /api/iam/tenants/{id}",
                        "DELETE /api/iam/tenants/{id}",
                        "GET /api/iam/users/detail",
                        "POST /api/agent/agents/status",
                        "POST /api/agent/versions/copy",
                        "GET /api/model/providers",
                        "GET /api/model/platforms",
                        "POST /api/model/platforms",
                        "GET /api/model/platforms/{id}",
                        "PUT /api/model/platforms/{id}",
                        "DELETE /api/model/platforms/{id}",
                        "GET /api/model/models",
                        "GET /api/model/model-configurations",
                        "POST /api/model/model-configurations",
                        "GET /api/model/model-configurations/{id}",
                        "PUT /api/model/model-configurations/{id}",
                        "DELETE /api/model/model-configurations/{id}")
                .doesNotContain(
                        "GET /api/education/course/list",
                        "GET /api/education/course/detail",
                        "POST /api/education/course/create",
                        "POST /api/education/course/update",
                        "POST /api/education/course/delete",
                        "GET /api/education/chapter/detail",
                        "POST /api/education/chapter/create",
                        "POST /api/education/chapter/update",
                        "POST /api/education/chapter/delete",
                        "GET /api/education/exam/list",
                        "GET /api/education/exam/detail",
                        "POST /api/education/exam/create",
                        "POST /api/education/exam/update",
                        "POST /api/education/exam/delete",
                        "GET /api/education/question/list",
                        "GET /api/education/question/detail",
                        "POST /api/education/question/create",
                        "POST /api/education/question/update",
                        "POST /api/education/question/delete",
                        "GET /api/education/resource/list",
                        "GET /api/education/resource/detail",
                        "POST /api/education/resource/create",
                        "POST /api/education/resource/update",
                        "POST /api/education/resource/delete",
                        "GET /api/education/review/list",
                        "GET /api/education/review/detail",
                        "POST /api/education/review/create",
                        "POST /api/education/review/update",
                        "POST /api/education/review/delete",
                        "GET /api/education/students/list",
                        "GET /api/education/students/detail",
                        "POST /api/education/students/create",
                        "POST /api/education/students/update",
                        "POST /api/education/students/delete",
                        "GET /api/education/study-plan/detail",
                        "POST /api/education/study-plan/create",
                        "POST /api/education/study-plan/update",
                        "POST /api/education/study-plan/delete",
                        "GET /api/education/subject/list",
                        "GET /api/education/subject/detail",
                        "POST /api/education/subject/create",
                        "POST /api/education/subject/update",
                        "POST /api/education/subject/delete",
                        "GET /api/education/textbook/list",
                        "GET /api/education/textbook/detail",
                        "POST /api/education/textbook/create",
                        "POST /api/education/textbook/update",
                        "POST /api/education/textbook/delete",
                        "GET /api/education/wrong-question/detail",
                        "POST /api/education/wrong-question/create",
                        "POST /api/education/wrong-question/update",
                        "POST /api/education/wrong-question/delete",
                        "GET /api/iam/users/list",
                        "POST /api/iam/users/create",
                        "POST /api/iam/users/update",
                        "POST /api/iam/users/delete",
                        "GET /api/iam/roles/list",
                        "GET /api/iam/roles/detail",
                        "POST /api/iam/roles/create",
                        "POST /api/iam/roles/update",
                        "POST /api/iam/roles/delete",
                        "GET /api/iam/menus/page",
                        "POST /api/iam/menus/create",
                        "POST /api/iam/menus/update",
                        "POST /api/iam/menus/delete",
                        "GET /api/iam/dicts/list",
                        "POST /api/iam/dicts/create",
                        "POST /api/iam/dicts/update",
                        "POST /api/iam/dicts/delete",
                        "GET /api/iam/auth-codes/page",
                        "POST /api/iam/auth-codes/create",
                        "POST /api/iam/auth-codes/update",
                        "POST /api/iam/auth-codes/delete",
                        "GET /api/agent/agents/page",
                        "GET /api/agent/agents/detail",
                        "POST /api/agent/agents/create",
                        "POST /api/agent/agents/update",
                        "POST /api/agent/agents/delete",
                        "GET /api/agent/agents/list",
                        "GET /api/agent/agents/detail/by-agent-id",
                        "POST /api/agent/agents/delete/by-agent-id",
                        "GET /api/agent/intents/page",
                        "GET /api/agent/intents/detail",
                        "POST /api/agent/intents/create",
                        "POST /api/agent/intents/update",
                        "POST /api/agent/intents/delete",
                        "POST /api/agent/intents/batch-delete",
                        "GET /api/iam/tenants/list",
                        "GET /api/iam/tenants/page",
                        "GET /api/iam/tenants/detail",
                        "POST /api/iam/tenants/create",
                        "POST /api/iam/tenants/update",
                        "POST /api/iam/tenants/delete",
                        "GET /api/agent/versions/list",
                        "GET /api/agent/versions/detail",
                        "POST /api/agent/versions/create",
                        "POST /api/agent/versions/update",
                        "POST /api/agent/versions/delete",
                        "GET /api/model/providers/page",
                        "GET /api/model/providers/detail",
                        "POST /api/model/providers/create",
                        "POST /api/model/providers/update",
                        "POST /api/model/providers/delete",
                        "GET /api/model/models/page",
                        "GET /api/model/models/detail",
                        "POST /api/model/models/create",
                        "POST /api/model/models/update",
                        "POST /api/model/models/delete");
    }

    private Set<String> openApiOperations(String path) throws Exception {
        HttpResponse<String> response = request("GET", path, null, null);
        assertThat(response.statusCode()).as(path).isEqualTo(200);
        String body = response.body();
        String exportPath = System.getProperty("shiyu.openapi.export");
        if ("/v3/api-docs".equals(path) && exportPath != null && !exportPath.isBlank()) {
            Files.writeString(Path.of(exportPath), body, StandardCharsets.UTF_8);
        }
        JsonNode paths = objectMapper.readTree(body).path("paths");
        assertThat(paths.isObject()).isTrue();
        Set<String> result = new TreeSet<>();
        Set<String> methods = Set.of("get", "post", "put", "patch", "delete", "head", "options", "trace");
        paths.properties().forEach(entry -> entry.getValue().fieldNames().forEachRemaining(method -> {
            if (methods.contains(method)) {
                result.add(method.toUpperCase(java.util.Locale.ROOT) + " " + normalizeRoute(entry.getKey()));
            }
        }));
        return result;
    }

    private static String normalizeRoute(String path) {
        return path.replaceAll("\\{([^{}:]+):[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}", "{$1}");
    }

    @Test
    void completesAuthenticatedAgentConversationAndKnowledgeJourney() throws Exception {
        HttpResponse<String> loginResponse =
                request(
                        "POST",
                        "/api/iam/auth/login",
                        null,
                        "{\"username\":\"admin\",\"password\":\"123456\"}");
        assertThat(loginResponse.statusCode()).isEqualTo(200);
        JsonNode login = body(loginResponse.body());
        assertThat(login.path("success").asBoolean()).isTrue();
        assertThat(login.path("code").asInt()).isEqualTo(200);
        String token = login.path("data").path("accessToken").asText();
        long tenantId = login.path("data").path("currentTenantId").asLong();
        assertThat(token).isNotBlank();
        assertThat(tenantId).isPositive();

        String authorization = "Bearer " + token;
        HttpResponse<String> appResponse =
                request(
                        "POST",
                        "/api/agent/apps",
                        authorization,
                        "{\"name\":\"core-journey\",\"description\":\"smoke\"}");
        assertThat(appResponse.statusCode()).isEqualTo(200);
        JsonNode app = body(appResponse.body());
        assertSuccess(app);
        assertThat(app.path("data").path("tenantId").path("value").asLong()).isEqualTo(tenantId);
        String appId = app.path("data").path("id").asText();
        assertThat(appId).isNotBlank();

        HttpResponse<String> appsResponse = request("GET", "/api/agent/apps", authorization, null);
        assertThat(appsResponse.statusCode()).isEqualTo(200);
        JsonNode apps = body(appsResponse.body());
        assertSuccess(apps);
        assertThat(apps.path("data").toString()).contains(appId);

        HttpResponse<String> conversationResponse =
                request(
                        "POST",
                        "/api/conversation/conversations",
                        authorization,
                        "{\"sceneType\":\"chat\",\"title\":\"core"
                                + " journey\",\"platform\":\"local\",\"model\":\"smoke\"}");
        assertThat(conversationResponse.statusCode()).isEqualTo(200);
        JsonNode conversation = body(conversationResponse.body());
        assertSuccess(conversation);
        assertThat(tenantValue(conversation.path("data"))).isEqualTo(tenantId);

        HttpResponse<String> conversationsResponse =
                request("GET", "/api/conversation/conversations", authorization, null);
        assertThat(conversationsResponse.statusCode()).isEqualTo(200);
        JsonNode conversations = body(conversationsResponse.body());
        assertSuccess(conversations);
        assertThat(conversations.path("data").toString()).contains("core journey");

        HttpResponse<String> spaceResponse =
                request("POST", "/api/knowledge/spaces/default", authorization, null);
        assertThat(spaceResponse.statusCode()).isEqualTo(200);
        JsonNode space = body(spaceResponse.body());
        assertSuccess(space);
        long spaceId = space.path("data").path("id").asLong();
        assertThat(spaceId).isPositive();
        assertThat(
                        jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM KNOWLEDGE_SPACE WHERE ID = ? AND TENANT_ID ="
                                        + " ?",
                                Integer.class,
                                spaceId,
                                tenantId))
                .isEqualTo(1);

        HttpResponse<String> spacesResponse =
                request("GET", "/api/knowledge/spaces/options", authorization, null);
        assertThat(spacesResponse.statusCode()).isEqualTo(200);
        JsonNode spaces = body(spacesResponse.body());
        assertSuccess(spaces);
        assertThat(spaces.path("data").toString()).contains(Long.toString(spaceId));
    }

    @Test
    void servesBundledConsoleAndKeepsConsoleAndBusinessCredentialsSeparate() throws Exception {
        URI base = URI.create("http://127.0.0.1:" + port);
        HttpResponse<String> page = httpClient.send(
                HttpRequest.newBuilder(base.resolve("/console/")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.headers().firstValue("content-type").orElse(""))
                .contains("text/html");
        assertThat(page.body()).contains("<div id=\"app\"></div>", "/console/assets/index-");

        HttpResponse<String> unauthenticated = httpClient.send(
                HttpRequest.newBuilder(base.resolve("/console/api/runtime")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(unauthenticated.statusCode()).isEqualTo(401);

        String grant = consoleSessions.issueOneTimeCode();
        HttpResponse<String> exchange = httpClient.send(
                HttpRequest.newBuilder(base.resolve("/console/api/session/exchange"))
                        .header("Origin", base.toString())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of("grant", grant))))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(exchange.statusCode()).isEqualTo(200);
        String cookie = exchange.headers().firstValue("set-cookie").orElseThrow().split(";", 2)[0];
        assertThat(exchange.headers().firstValue("set-cookie").orElseThrow().toLowerCase())
                .contains("httponly", "samesite=strict", "path=/console")
                .doesNotContain("max-age");
        String csrf = body(exchange.body()).path("csrfToken").asText();
        assertThat(csrf).isNotBlank();

        HttpResponse<String> noCsrf = httpClient.send(
                HttpRequest.newBuilder(base.resolve("/console/api/config/validate"))
                        .header("Origin", base.toString())
                        .header("Cookie", cookie)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"expectedVersion\":0,\"values\":{},\"secrets\":{}}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(noCsrf.statusCode()).isEqualTo(403);

        HttpResponse<String> validCsrf = httpClient.send(
                HttpRequest.newBuilder(base.resolve("/console/api/config/validate"))
                        .header("Origin", base.toString())
                        .header("Cookie", cookie)
                        .header("X-ShiYu-Console-CSRF", csrf)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"expectedVersion\":0,\"values\":{},\"secrets\":{}}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(validCsrf.statusCode()).isEqualTo(200);
        assertThat(body(validCsrf.body()).path("valid").asBoolean()).isTrue();

        HttpResponse<String> runtime = httpClient.send(
                HttpRequest.newBuilder(base.resolve("/console/api/runtime")).header("Cookie", cookie).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(runtime.statusCode()).isEqualTo(200);
        assertThat(body(runtime.body()).path("port").asInt()).isEqualTo(port);

        HttpResponse<String> login = request(
                "POST", "/api/iam/auth/login", null, "{\"username\":\"admin\",\"password\":\"123456\"}");
        assertThat(body(login.body()).path("success").asBoolean()).isTrue();
        String businessToken = body(login.body()).path("data").path("accessToken").asText();
        HttpResponse<String> businessTokenCannotManageConsole = httpClient.send(
                HttpRequest.newBuilder(base.resolve("/console/api/runtime"))
                        .header("Authorization", "Bearer " + businessToken)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(businessTokenCannotManageConsole.statusCode()).isEqualTo(401);

        HttpResponse<String> consoleSessionCannotAccessBusinessApi = httpClient.send(
                HttpRequest.newBuilder(base.resolve("/api/iam/users"))
                        .header("Cookie", cookie)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(body(consoleSessionCannotAccessBusinessApi.body()).path("success").asBoolean()).isFalse();
        assertThat(body(consoleSessionCannotAccessBusinessApi.body()).path("code").asInt()).isEqualTo(401);
    }

    private JsonNode body(String value) throws Exception {
        return objectMapper.readTree(value);
    }

    private HttpResponse<String> request(
            String method, String path, String authorization, String body) throws Exception {
        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + path))
                        .header("Accept", "application/json");
        if (authorization != null) builder.header("Authorization", authorization);
        if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
        else
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body));
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void assertSuccess(JsonNode response) {
        assertThat(response.path("success").asBoolean()).isTrue();
        assertThat(response.path("code").asInt()).isEqualTo(200);
    }

    private long tenantValue(JsonNode data) {
        JsonNode tenant = data.path("tenantId");
        return tenant.has("value") ? tenant.path("value").asLong() : tenant.asLong();
    }
}
