package com.shiyu.ai.iam.implementation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.shiyu.ai.iam.implementation.request.RoleRequest;

import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.Test;

/**
 * 验证 角色 Request Validation 相关功能、边界条件、异常路径和协作行为。
 */
class RoleRequestValidationTest {

    @Test
    void tenantIsRequiredForRoleCommands() throws NoSuchFieldException {
        assertNotNull(
                RoleRequest.class.getDeclaredField("tenantId").getAnnotation(NotNull.class),
                "role commands must not fall back to the ambient tenant");
    }
}
