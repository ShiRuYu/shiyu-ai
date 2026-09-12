package com.shiyu.ai.iam.implementation.application.assembler;

import com.shiyu.ai.iam.implementation.api.request.AuthCodeRequest;
import com.shiyu.ai.iam.implementation.api.response.AuthCodeResponse;
import com.shiyu.ai.iam.implementation.domain.model.AuthCodeBO;

/**
 * {@code AuthCodeAssembler} 承载平台模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public final class AuthCodeAssembler {
    private AuthCodeAssembler() {}

    /**
     * {@code toBO} 将当前对象转换为目标表示形式。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static AuthCodeBO toBO(AuthCodeRequest request) {
        if (request == null) return null;
        AuthCodeBO bo = new AuthCodeBO();
        bo.setCode(request.getCode());
        bo.setName(request.getName());
        return bo;
    }

    /**
     * {@code toResponse} 将当前对象转换为目标表示形式。
     *
     * @param bo 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
