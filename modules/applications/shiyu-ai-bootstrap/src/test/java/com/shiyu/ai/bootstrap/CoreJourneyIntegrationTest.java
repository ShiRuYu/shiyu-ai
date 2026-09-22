package com.shiyu.ai.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.URI;
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
    }

    @Autowired @LocalServerPort private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private JdbcTemplate jdbcTemplate;

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
                mapping.getPatternValues().forEach(path ->
                        mapping.getMethodsCondition().getMethods().forEach(method ->
                                expected.add(method.name() + " " + normalizeRoute(path))));
            }
        });
        assertThat(expected).isNotEmpty();
        Set<String> actual = openApiOperations("/v3/api-docs");
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
                if (handler.getBeanType().getPackageName().startsWith(group.getValue())) {
                    mapping.getPatternValues().forEach(path ->
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
