package com.shiyu.ai.runtimeconsole.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import com.shiyu.ai.runtimeconsole.auth.ConsoleSessionStore;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;

/** 覆盖运行时生命周期操作的启动器授权边界。 */
class RuntimeLifecycleControllerTest {

    @AfterEach
    void clearLauncherMode() {
        System.clearProperty("shiyu.console.launcher-managed");
    }

    @Test
    void refusesToStopAnIdeaOrDirectJarProcess() {
        System.clearProperty("shiyu.console.launcher-managed");
        RuntimeLifecycleController controller = new RuntimeLifecycleController(
                new LauncherControlClient(-1, ""), mock(ConfigurableApplicationContext.class),
                new ConsoleSessionStore(java.time.Clock.systemUTC()), mock(Environment.class));

        ResponseStatusException failure = assertThrows(ResponseStatusException.class, controller::stop);

        assertEquals(409, failure.getStatusCode().value());
    }

    @Test
    void requiresAnAuthenticatedParentControlChannelForManagedActions() {
        System.setProperty("shiyu.console.launcher-managed", "true");
        RuntimeLifecycleController controller = new RuntimeLifecycleController(
                new LauncherControlClient(-1, ""), mock(ConfigurableApplicationContext.class),
                new ConsoleSessionStore(java.time.Clock.systemUTC()), mock(Environment.class));

        ResponseStatusException failure = assertThrows(ResponseStatusException.class, controller::restart);

        assertEquals(503, failure.getStatusCode().value());
    }
}
