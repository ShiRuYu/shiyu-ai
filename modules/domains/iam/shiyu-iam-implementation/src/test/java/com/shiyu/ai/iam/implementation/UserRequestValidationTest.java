package com.shiyu.ai.iam.implementation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.shiyu.ai.iam.implementation.request.UserRequest;

import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.Test;

class UserRequestValidationTest {

    @Test
    void tenantIsRequiredForUserCommands() throws NoSuchFieldException {
        assertNotNull(
                UserRequest.class.getDeclaredField("tenantId").getAnnotation(NotNull.class),
                "user commands must not fall back to the ambient tenant");
    }
}
