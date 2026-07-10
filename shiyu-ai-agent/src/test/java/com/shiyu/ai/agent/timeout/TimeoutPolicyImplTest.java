package com.shiyu.ai.agent.timeout;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TimeoutPolicyImpl 单元测试
 */
@Tag("dev")
class TimeoutPolicyImplTest {

    private TimeoutPolicyImpl timeoutPolicy;
    private TimeoutConfig config;

    @BeforeEach
    void setUp() {
        timeoutPolicy = new TimeoutPolicyImpl();
        config = new TimeoutConfig(5000, 100); // 100ms 节点超时
    }

    @Test
    void testExecuteWithinTimeout() throws Exception {
        String result = timeoutPolicy.executeWithTimeout(() -> "fast result", config);
        assertEquals("fast result", result);
    }

    @Test
    void testExecuteTimeout() {
        Callable<String> slowTask = () -> {
            Thread.sleep(500); // 超过 100ms 超时
            return "too late";
        };

        assertThrows(TimeoutException.class, () ->
            timeoutPolicy.executeWithTimeout(slowTask, config)
        );
    }

    @Test
    void testExecuteExceptionPropagation() {
        Callable<String> failingTask = () -> {
            throw new IllegalArgumentException("custom error");
        };

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            timeoutPolicy.executeWithTimeout(failingTask, config)
        );
        assertEquals("custom error", exception.getMessage());
    }

    @Test
    void testDefaultConfig() {
        TimeoutConfig defaultConfig = TimeoutConfig.defaultConfig();
        assertEquals(300000, defaultConfig.getGlobalTimeoutMs());
        assertEquals(60000, defaultConfig.getNodeTimeoutMs());
    }
}
