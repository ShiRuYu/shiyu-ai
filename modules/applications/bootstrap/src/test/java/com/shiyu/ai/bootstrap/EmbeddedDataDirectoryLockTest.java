package com.shiyu.ai.bootstrap;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddedDataDirectoryLockTest {

    @Test
    void shouldResolveAppHomeUsingStartupPrecedence() {
        assertThat(EmbeddedDataDirectoryLock.resolveAppHome("system-home", "env-home", "fallback"))
                .isEqualTo("system-home");
        assertThat(EmbeddedDataDirectoryLock.resolveAppHome(" ", "env-home", "fallback"))
                .isEqualTo("env-home");
        assertThat(EmbeddedDataDirectoryLock.resolveAppHome(null, " ", "fallback"))
                .isEqualTo("fallback");
        assertThat(EmbeddedDataDirectoryLock.resolveAppHome(null, null, " "))
                .isEqualTo(".");
    }

    @Test
    void shouldPreferLocalRuntimeDirectoryWhenNoExplicitHomeIsSet() throws Exception {
        Path project = Files.createTempDirectory("shiyu-app-home-");
        Files.createDirectories(project.resolve("runtime/dev"));
        assertThat(EmbeddedDataDirectoryLock.resolveDefaultAppHome(project.toString()))
                .isEqualTo(project.resolve("runtime/dev").toString());
        assertThat(EmbeddedDataDirectoryLock.resolveDefaultAppHome(project.resolve("missing").toString()))
                .isEqualTo(project.resolve("missing").toString());
    }
}
