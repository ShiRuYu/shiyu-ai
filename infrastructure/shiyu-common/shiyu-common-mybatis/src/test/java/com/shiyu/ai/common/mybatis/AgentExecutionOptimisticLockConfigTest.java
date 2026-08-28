package com.shiyu.ai.dal.agent.repository;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the shipped MyBatis-Flex configuration against treating the
 * {@code agent_execution.version} business column (the Agent version number,
 * e.g. {@code v2}) as an optimistic-lock column.
 */
class AgentExecutionOptimisticLockConfigTest {

    @Test
    void globalVersionColumnDoesNotShadowAgentExecutionVersion() throws Exception {
        String yaml = loadCommonMybatisConfig();

        assertFalse(yaml.contains("version-column: version"),
                "agent_execution.version stores the Agent version number; it must not be "
                        + "configured as the global MyBatis-Flex optimistic-lock column");
        assertTrue(yaml.contains("tenant-column: tenantId"),
                "tenant isolation configuration must stay intact");
    }

    private static String loadCommonMybatisConfig() throws Exception {
        try (InputStream input = AgentExecutionOptimisticLockConfigTest.class
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
