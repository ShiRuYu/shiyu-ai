package com.shiyu.ai.common.mybatis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 验证 AgentExecutionOptimisticLockConfig 的功能、边界条件和集成行为。
 */
class AgentExecutionOptimisticLockConfigTest {

    @Test
    void globalVersionColumnDoesNotShadowAgentExecutionVersion() throws Exception {
        String yaml = loadCommonMybatisConfig();

        assertFalse(
                yaml.contains("version-column: version"),
                "agent_execution.version stores the Agent version number; it must not be "
                        + "configured as the global MyBatis-Flex optimistic-lock column");
        assertTrue(
                yaml.contains("tenant-column: tenantId"),
                "tenant isolation configuration must stay intact");
    }

    private static String loadCommonMybatisConfig() throws Exception {
        try (InputStream input =
                AgentExecutionOptimisticLockConfigTest.class
                        .getClassLoader()
                        .getResourceAsStream("application-common-mybatis.yml")) {
            if (input == null) {
                throw new IllegalStateException(
                        "application-common-mybatis.yml is not on the test classpath");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
