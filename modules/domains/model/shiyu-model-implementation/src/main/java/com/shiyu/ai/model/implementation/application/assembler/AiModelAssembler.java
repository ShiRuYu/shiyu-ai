package com.shiyu.ai.model.implementation.application.assembler;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.model.implementation.domain.model.AiModelBO;
import com.shiyu.ai.model.implementation.web.request.AiModelRequest;
import com.shiyu.ai.model.implementation.web.response.AiModelResponse;

/**
 * {@code AiModelAssembler} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public final class AiModelAssembler {
    private AiModelAssembler() {}

    /**
     * {@code toBO} 将当前对象转换为目标表示形式。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static AiModelBO toBO(AiModelRequest request) {
        return MapstructUtils.convert(request, AiModelBO.class);
    }

    /**
     * {@code toResponse} 将当前对象转换为目标表示形式。
     *
     * @param bo 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static AiModelResponse toResponse(AiModelBO bo) {
        return MapstructUtils.convert(bo, AiModelResponse.class);
    }
}
