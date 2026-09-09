package com.shiyu.ai.iam.implementation.application.assembler;

import com.shiyu.ai.iam.implementation.api.request.AuthCodeRequest;
import com.shiyu.ai.iam.implementation.api.response.AuthCodeResponse;
import com.shiyu.ai.iam.implementation.domain.model.AuthCodeBO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthCodeAssemblerTest {
    @Test
    void handlesNullAndCopiesFields() {
        assertNull(AuthCodeAssembler.toBO(null));
        assertNull(AuthCodeAssembler.toResponse(null));

        AuthCodeRequest request = new AuthCodeRequest();
        request.setCode("agent.read");
        request.setName("Agent read");
        AuthCodeBO bo = AuthCodeAssembler.toBO(request);
        assertEquals("agent.read", bo.getCode());
        assertEquals("Agent read", bo.getName());
        bo.setId(3L);
        bo.setStatus(1);
        AuthCodeResponse response = AuthCodeAssembler.toResponse(bo);
        assertEquals(3L, response.getId());
        assertEquals("agent.read", response.getCode());
        assertEquals(1, response.getStatus());
    }
}

