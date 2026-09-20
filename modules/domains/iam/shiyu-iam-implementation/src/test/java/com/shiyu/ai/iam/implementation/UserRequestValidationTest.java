package com.shiyu.ai.iam.implementation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.shiyu.ai.iam.implementation.request.UserRequest;

import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.Test;

/**
 * 验证 用户 Request Validation 相关功能、边界条件、异常路径和协作行为。
 */
class UserRequestValidationTest {

    @Test
    void tenantIsRequiredForUserCommands() throws NoSuchFieldException {
        assertNotNull(
                UserRequest.class.getDeclaredField("tenantId").getAnnotation(NotNull.class),
                "user commands must not fall back to the ambient tenant");
    }
}
