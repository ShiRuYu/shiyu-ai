package com.shiyu.ai.web.agent;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.agent.web.AgentDefinitionController;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("dev")
class AgentDefinitionControllerSecurityTest {

    @Test
    void switchingVersionRequiresEditPermission() throws NoSuchMethodException {
        SaCheckPermission permission = AgentDefinitionController.class
                .getMethod("switchVersion", String.class, String.class)
                .getAnnotation(SaCheckPermission.class);

        assertThat(permission).isNotNull();
        assertThat(permission.value()).containsExactly("agent:admin:edit");
    }
}
