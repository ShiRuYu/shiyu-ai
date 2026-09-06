package com.shiyu.ai.auth;

import com.shiyu.ai.auth.request.UserRequest;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserRequestValidationTest {

    @Test
    void tenantIsRequiredForUserCommands() throws NoSuchFieldException {
        assertNotNull(
                UserRequest.class.getDeclaredField("tenantId").getAnnotation(NotNull.class),
                "user commands must not fall back to the ambient tenant"
        );
    }
}
