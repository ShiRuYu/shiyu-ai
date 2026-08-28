package com.shiyu.ai.auth;

import com.shiyu.ai.auth.request.RoleRequest;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoleRequestValidationTest {

    @Test
    void tenantIsRequiredForRoleCommands() throws NoSuchFieldException {
        assertNotNull(
                RoleRequest.class.getDeclaredField("tenantId").getAnnotation(NotNull.class),
                "role commands must not fall back to the ambient tenant"
        );
    }
}
