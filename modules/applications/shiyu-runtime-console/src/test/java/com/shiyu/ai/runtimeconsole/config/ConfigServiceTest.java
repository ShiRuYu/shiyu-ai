package com.shiyu.ai.runtimeconsole.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class ConfigServiceTest {

    @TempDir Path appHome;

    @Test
    void validatesPortAndProviderCompatibilityBeforeWritingAnything() {
        MockEnvironment environment = baseEnvironment();
        try (Fixture fixture = new Fixture(environment)) {
            var invalid = fixture.service.validate(new ConfigChangeSet(
                    0,
                    Map.of("server.port", "70000", "shiyu.vector-store.type", "pgvector"),
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
