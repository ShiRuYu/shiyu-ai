package com.shiyu.ai.auth.application.assembler;

import com.shiyu.ai.auth.api.request.AuthCodeRequest;
import com.shiyu.ai.auth.api.response.AuthCodeResponse;
import com.shiyu.ai.auth.domain.model.AuthCodeBO;

public final class AuthCodeAssembler {
    private AuthCodeAssembler() {}

    public static AuthCodeBO toBO(AuthCodeRequest request) {
        if (request == null) return null;
        AuthCodeBO bo = new AuthCodeBO();
        bo.setCode(request.getCode());
        bo.setName(request.getName());
        return bo;
    }

    public static AuthCodeResponse toResponse(AuthCodeBO bo) {
        if (bo == null) return null;
        AuthCodeResponse response = new AuthCodeResponse();
        response.setId(bo.getId());
        response.setCode(bo.getCode());
        response.setName(bo.getName());
        response.setStatus(bo.getStatus());
        response.setCreateTime(bo.getCreateTime());
        response.setUpdateTime(bo.getUpdateTime());
        return response;
    }
}
