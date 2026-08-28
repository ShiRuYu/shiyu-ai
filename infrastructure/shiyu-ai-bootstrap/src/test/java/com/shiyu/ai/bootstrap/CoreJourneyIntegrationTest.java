package com.shiyu.ai.bootstrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiyu.ai.common.core.utils.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(
        classes = ShiyuBootstrapApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoreJourneyIntegrationTest {

    private static final Path APP_HOME = Path.of(
            System.getProperty("java.io.tmpdir"),
            "shiyu-core-journey-" + UUID.randomUUID());

    @DynamicPropertySource
    static void appHome(DynamicPropertyRegistry registry) {
        registry.add("app.home", () -> APP_HOME.toString());
        registry.add("spring.profiles.active", () -> "dev");
    }

    @Autowired
    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setKnownPassword() throws Exception {
        Files.createDirectories(APP_HOME);
        jdbcTemplate.update(
                "UPDATE AUTH_USER SET PASSWORD = ? WHERE USERNAME = 'admin'",
                PasswordUtils.encode("smoke-password"));
    }

    @Test
    void completesAuthenticatedAgentConversationAndKnowledgeJourney() throws Exception {
        HttpResponse<String> loginResponse = request("POST", "/api/iam/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"smoke-password\"}");
        assertThat(loginResponse.statusCode()).isEqualTo(200);
        JsonNode login = body(loginResponse.body());
        assertThat(login.path("success").asBoolean()).isTrue();
        assertThat(login.path("code").asInt()).isEqualTo(200);
        String token = login.path("data").path("accessToken").asText();
        long tenantId = login.path("data").path("currentTenantId").asLong();
        assertThat(token).isNotBlank();
        assertThat(tenantId).isPositive();

        String authorization = "Bearer " + token;
        HttpResponse<String> appResponse = request("POST", "/api/agent/apps", authorization,
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

        HttpResponse<String> conversationResponse = request("POST", "/api/conversation/conversations", authorization,
                "{\"sceneType\":\"chat\",\"title\":\"core journey\",\"platform\":\"local\",\"model\":\"smoke\"}");
        assertThat(conversationResponse.statusCode()).isEqualTo(200);
        JsonNode conversation = body(conversationResponse.body());
        assertSuccess(conversation);
        assertThat(tenantValue(conversation.path("data"))).isEqualTo(tenantId);

        HttpResponse<String> conversationsResponse = request("GET", "/api/conversation/conversations", authorization, null);
        assertThat(conversationsResponse.statusCode()).isEqualTo(200);
        JsonNode conversations = body(conversationsResponse.body());
        assertSuccess(conversations);
        assertThat(conversations.path("data").toString()).contains("core journey");

        HttpResponse<String> spaceResponse = request("POST", "/api/knowledge/spaces/default", authorization, null);
        assertThat(spaceResponse.statusCode()).isEqualTo(200);
        JsonNode space = body(spaceResponse.body());
        assertSuccess(space);
        long spaceId = space.path("data").path("id").asLong();
        assertThat(spaceId).isPositive();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM KNOWLEDGE_SPACE WHERE ID = ? AND TENANT_ID = ?",
                Integer.class, spaceId, tenantId)).isEqualTo(1);

        HttpResponse<String> spacesResponse = request("GET", "/api/knowledge/spaces/options", authorization, null);
        assertThat(spacesResponse.statusCode()).isEqualTo(200);
        JsonNode spaces = body(spacesResponse.body());
        assertSuccess(spaces);
        assertThat(spaces.path("data").toString()).contains(Long.toString(spaceId));
    }

    private JsonNode body(String value) throws Exception {
        return objectMapper.readTree(value);
    }

    private HttpResponse<String> request(String method, String path, String authorization, String body)
            throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Accept", "application/json");
        if (authorization != null) builder.header("Authorization", authorization);
        if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
        else builder.header("Content-Type", "application/json")
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
