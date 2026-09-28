package com.shiyu.ai.runtimeconsole.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiyu.ai.runtimeconsole.metrics.ConsoleHttpMetrics;
import com.shiyu.ai.runtimeconsole.metrics.ConsoleMetricsSampler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

/** 覆盖运行时配置校验、保存、恢复及密钥处理行为。 */
class ConfigServiceTest {

    @TempDir Path appHome;

    @Test
    void validatesPortAndProviderCompatibilityBeforeWritingAnything() {
        MockEnvironment environment = baseEnvironment();
        try (Fixture fixture = new Fixture(environment)) {
            var invalid = fixture.service.validate(new ConfigChangeSet(
                    0,
                    Map.of("server.port", "70000", "shiyu.infrastructure.vector.provider", "pgvector"),
                    Map.of()));

            assertTrue(invalid.stream().anyMatch(issue -> issue.contains("端口")));
            assertTrue(invalid.stream().anyMatch(issue -> issue.contains("PostgreSQL")));
            assertEquals(0, fixture.store.current().revision());
        }
    }

    @Test
    void immediateChangesApplyNowWhileRestartChangesRemainPending() {
        MockEnvironment environment = baseEnvironment();
        try (Fixture fixture = new Fixture(environment)) {
            ConfigApplyResult immediate = fixture.service.save(new ConfigChangeSet(
                    0, Map.of("shiyu.console.sample-interval-ms", "10000"), Map.of()));
            assertEquals("APPLIED", immediate.status());
            assertEquals(10_000, fixture.sampler.sampleIntervalMillis());

            ConfigApplyResult restart = fixture.service.save(new ConfigChangeSet(
                    1, Map.of("server.port", "9001"), Map.of()));
            assertEquals("PENDING_RESTART", restart.status());
            assertTrue(restart.restartRequired());
        }
    }

    @Test
    void databaseEditsUseTheDatasourcePropertiesThatTheApplicationActuallyBinds() {
        MockEnvironment environment = baseEnvironment()
                .withProperty("mybatis-flex.datasource.agent.url", "jdbc:h2:mem:shiyu")
                .withProperty("mybatis-flex.datasource.agent.username", "sa");
        try (Fixture fixture = new Fixture(environment)) {
            var fields = fixture.service.describe().fields();
            assertTrue(fields.stream().anyMatch(field -> field.key().equals("mybatis-flex.datasource.agent.url")));
            assertFalse(fields.stream().anyMatch(field -> field.key().equals("spring.datasource.url")));

            var issues = fixture.service.validate(new ConfigChangeSet(
                    0,
                    Map.of("mybatis-flex.datasource.agent.url", "jdbc:h2:mem:next"),
                    Map.of()));
            assertTrue(issues.isEmpty(), () -> String.join("; ", issues));
            assertTrue(fixture.service.validate(new ConfigChangeSet(
                    0, Map.of("spring.datasource.url", "jdbc:h2:mem:ignored"), Map.of()))
                    .stream().anyMatch(issue -> issue.contains("不允许修改字段")));
        }
    }

    @Test
    void fileAndVectorProviderDescriptorsReflectTheProviderOverridesUsedByComposition() {
        MockEnvironment environment = baseEnvironment()
                .withProperty("shiyu.infrastructure.database.provider", "postgresql")
                .withProperty("shiyu.infrastructure.file.provider", "s3")
                .withProperty("shiyu.infrastructure.vector.provider", "pgvector")
                .withProperty("mybatis-flex.datasource.agent.url", "jdbc:postgresql://localhost/shiyu");
        environment.setActiveProfiles("postgresql", "s3", "external-infra");
        try (Fixture fixture = new Fixture(environment)) {
            var fields = fixture.service.describe().fields();
            assertEquals("s3", fields.stream()
                    .filter(field -> field.key().equals("shiyu.infrastructure.file.provider"))
                    .findFirst().orElseThrow().effectiveValue());
            assertEquals("pgvector", fields.stream()
                    .filter(field -> field.key().equals("shiyu.infrastructure.vector.provider"))
                    .findFirst().orElseThrow().effectiveValue());
        }
    }

    @Test
    void rejectedImmediateSaveRestoresThePreviouslyAppliedRuntimeValue() {
        MockEnvironment environment = baseEnvironment();
        try (Fixture fixture = new Fixture(environment)) {
            fixture.service.save(new ConfigChangeSet(
                    0, Map.of("shiyu.console.sample-interval-ms", "10000"), Map.of()));

            assertThrows(ConfigSnapshotStore.VersionConflictException.class, () -> fixture.service.save(
                    new ConfigChangeSet(0, Map.of("shiyu.console.sample-interval-ms", "20000"), Map.of())));

            assertEquals(10_000, fixture.sampler.sampleIntervalMillis());
        }
    }

    @Test
    void externalDatasourceOverrideIsReportedAndCannotBeEditedFromTheConsole() {
        String previous = System.getProperty("mybatis-flex.datasource.agent.url");
        System.setProperty("mybatis-flex.datasource.agent.url", "jdbc:postgresql://external/shiyu");
        try (Fixture fixture = new Fixture(baseEnvironment())) {
            ConfigFieldDescriptor url = fixture.service.describe().fields().stream()
                    .filter(field -> field.key().equals("mybatis-flex.datasource.agent.url"))
                    .findFirst().orElseThrow();

            assertEquals("系统属性", url.source());
            assertFalse(url.editable());
        } finally {
            if (previous == null) System.clearProperty("mybatis-flex.datasource.agent.url");
            else System.setProperty("mybatis-flex.datasource.agent.url", previous);
        }
    }

    @Test
    void sensitiveDescriptorsNeverRevealSavedSecretAndKeepDoesNotOverwriteIt() {
        MockEnvironment environment = baseEnvironment();
        try (Fixture fixture = new Fixture(environment)) {
            fixture.service.save(new ConfigChangeSet(
                    0,
                    Map.of(),
                    Map.of("shiyu.ai.openai.api-key", new ConfigChangeSet.SecretChange("replace", "do-not-show"))));
            ConfigFieldDescriptor key = fixture.service.describe().fields().stream()
                    .filter(field -> field.key().equals("shiyu.ai.openai.api-key"))
                    .findFirst().orElseThrow();

            assertTrue(key.sensitive());
            assertTrue(key.configured());
            assertFalse(key.effectiveValue().contains("do-not-show"));
            assertFalse(key.savedValue().contains("do-not-show"));
            assertEquals("已设置", key.effectiveValue());
        }
    }

    private MockEnvironment baseEnvironment() {
        return new MockEnvironment()
                .withProperty("server.port", "9000")
                .withProperty("shiyu.console.sample-interval-ms", "5000")
                .withProperty("logging.level.com.shiyu", "INFO")
                .withProperty("shiyu.infrastructure.database.provider", "h2")
                .withProperty("shiyu.vector-store.type", "jvector")
                .withProperty("shiyu.storage.type", "local")
                .withProperty("shiyu.infrastructure.event.provider", "in-process")
                .withProperty("shiyu.infrastructure.redis.provider", "disabled");
    }

    /** 为配置服务测试构造隔离的快照、环境和指标组件。 */
    private final class Fixture implements AutoCloseable {
        private final ConfigSnapshotStore store = new ConfigSnapshotStore(
                appHome, new ObjectMapper(), new ConfigSecretProtector() {
                    @Override public String protect(String plaintext) {
                        return "cipher:" + Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8));
                    }
                    @Override public String unprotect(String ciphertext) {
                        return new String(Base64.getDecoder().decode(ciphertext.substring(7)), StandardCharsets.UTF_8);
                    }
                });
        private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        private final ConsoleMetricsSampler sampler = new ConsoleMetricsSampler(
                appHome, null, new ConsoleHttpMetrics(registry), registry, 60_000);
        private final ConfigService service;

        private Fixture(MockEnvironment environment) {
            this.service = new ConfigService(store, environment, new RuntimeConfigApplier(sampler));
        }

        @Override public void close() {
            sampler.close();
            registry.close();
        }
    }
}
