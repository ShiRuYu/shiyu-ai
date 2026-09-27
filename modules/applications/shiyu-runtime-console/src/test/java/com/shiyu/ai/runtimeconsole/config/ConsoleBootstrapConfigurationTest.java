package com.shiyu.ai.runtimeconsole.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConsoleBootstrapConfigurationTest {

    @TempDir Path appHome;

    @AfterEach
    void restoreProcessProperties() {
        System.clearProperty("app.home");
        System.clearProperty("shiyu.infrastructure.database.provider");
        System.clearProperty("mybatis-flex.datasource.agent.url");
        System.clearProperty("spring.profiles.active");
        System.clearProperty("spring.profiles.include");
        ConsoleBootstrapConfiguration.resetForTests();
    }

    @Test
    void savedDatabaseProviderSelectsItsRealProfileWithoutDiscardingLauncherArguments() {
        System.setProperty("app.home", appHome.toString());
        ConfigSnapshotStore store = new ConfigSnapshotStore(appHome, new ObjectMapper(), new DpapiSecretProtector());
        store.save(0,
                Map.of(
                        "shiyu.infrastructure.database.provider", "postgresql",
                        "mybatis-flex.datasource.agent.url", "jdbc:postgresql://127.0.0.1/shiyu"),
                Map.of(),
                Set.of());

        String[] prepared = ConsoleBootstrapConfiguration.loadPersistedValues(
                new String[] {"--spring.profiles.active=windows", "--server.port=9123"});

        assertTrue(java.util.Arrays.asList(prepared).contains("--spring.profiles.active=windows"));
        assertTrue(java.util.Arrays.asList(prepared).contains("--server.port=9123"));
        assertTrue(java.util.Arrays.asList(prepared).contains("--spring.profiles.include=postgresql"));
        assertEquals("postgresql", System.getProperty("shiyu.infrastructure.database.provider"));
        assertEquals("jdbc:postgresql://127.0.0.1/shiyu", System.getProperty("mybatis-flex.datasource.agent.url"));
    }

    @Test
    void explicitlySelectedDatabaseProfileOverridesTheSavedProvider() {
        System.setProperty("app.home", appHome.toString());
        ConfigSnapshotStore store = new ConfigSnapshotStore(appHome, new ObjectMapper(), new DpapiSecretProtector());
        store.save(0, Map.of("shiyu.infrastructure.database.provider", "mysql"), Map.of(), Set.of());

        String[] prepared = ConsoleBootstrapConfiguration.loadPersistedValues(
                new String[] {"--spring.profiles.active=postgresql,external-infra"});

        assertEquals("postgresql", System.getProperty("shiyu.infrastructure.database.provider"));
        assertEquals("命令行", ConsoleBootstrapConfiguration.sourceOf("shiyu.infrastructure.database.provider"));
        assertFalse(java.util.Arrays.asList(prepared).contains("--spring.profiles.include=mysql"));
    }

    @Test
    void derivedDatabaseProfilePreservesOtherExplicitIncludedProfiles() {
        System.setProperty("app.home", appHome.toString());
        ConfigSnapshotStore store = new ConfigSnapshotStore(appHome, new ObjectMapper(), new DpapiSecretProtector());
        store.save(0, Map.of("shiyu.infrastructure.database.provider", "mysql"), Map.of(), Set.of());

        String[] prepared = ConsoleBootstrapConfiguration.loadPersistedValues(new String[] {
                "--spring.profiles.active=windows",
                "--spring.profiles.include=observability"
        });

        assertTrue(java.util.Arrays.asList(prepared).contains("--spring.profiles.include=observability,mysql"));
    }
}
