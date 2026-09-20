package com.shiyu.ai.model.implementation.application.assembler;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.model.implementation.domain.model.AiModelBO;
import com.shiyu.ai.model.implementation.web.request.AiModelRequest;
import com.shiyu.ai.model.implementation.web.response.AiModelResponse;

/**
 * 将 AI 模型 在不同层之间进行适配、转换或组装。
 */
public final class AiModelAssembler {
    private AiModelAssembler() {}

    /**
     * 构建或转换 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 AI 模型 相关操作生成的结果数据。
     */
    public static AiModelBO toBO(AiModelRequest request) {
        return MapstructUtils.convert(request, AiModelBO.class);
    }

    /**
     * 构建或转换 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 AI 模型 相关操作生成的结果数据。
     */
    public static AiModelResponse toResponse(AiModelBO bo) {
        return MapstructUtils.convert(bo, AiModelResponse.class);
    }
}
