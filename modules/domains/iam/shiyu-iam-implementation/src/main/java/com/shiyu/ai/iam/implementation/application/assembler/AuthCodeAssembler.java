package com.shiyu.ai.iam.implementation.application.assembler;

import com.shiyu.ai.iam.implementation.api.request.AuthCodeRequest;
import com.shiyu.ai.iam.implementation.api.response.AuthCodeResponse;
import com.shiyu.ai.iam.implementation.domain.model.AuthCodeBO;

/**
 * 将 认证 Code 在不同层之间进行适配、转换或组装。
 */
public final class AuthCodeAssembler {
    private AuthCodeAssembler() {}

    /**
     * 构建或转换 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 认证 Code 相关操作生成的结果数据。
     */
    public static AuthCodeBO toBO(AuthCodeRequest request) {
        if (request == null) return null;
        AuthCodeBO bo = new AuthCodeBO();
        bo.setCode(request.getCode());
        bo.setName(request.getName());
        return bo;
    }

    /**
     * 构建或转换 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 认证 Code 相关操作生成的结果数据。
     */
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
