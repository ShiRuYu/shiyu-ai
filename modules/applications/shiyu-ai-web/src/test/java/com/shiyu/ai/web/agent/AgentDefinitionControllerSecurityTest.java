package com.shiyu.ai.web.agent;

import static org.assertj.core.api.Assertions.assertThat;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.agent.implementation.web.AgentDefinitionController;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 验证 智能体 Definition Controller 安全 相关功能、边界条件、异常路径和协作行为。
 */
@Tag("dev")
class AgentDefinitionControllerSecurityTest {

    @Test
    void switchingVersionRequiresEditPermission() throws NoSuchMethodException {
        SaCheckPermission permission =
                AgentDefinitionController.class
                        .getMethod("switchVersion", String.class, String.class)
                        .getAnnotation(SaCheckPermission.class);

        assertThat(permission).isNotNull();
        assertThat(permission.value()).containsExactly("agent:admin:edit");
    }
}
