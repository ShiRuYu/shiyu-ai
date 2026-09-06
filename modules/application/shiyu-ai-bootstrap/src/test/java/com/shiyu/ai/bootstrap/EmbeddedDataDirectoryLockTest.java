package com.shiyu.ai.bootstrap;

import org.junit.jupiter.api.Test;

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
}
