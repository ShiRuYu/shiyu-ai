package com.shiyu.ai.model.implementation.application.assembler;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.model.implementation.domain.model.AiModelBO;
import com.shiyu.ai.model.implementation.web.request.AiModelRequest;
import com.shiyu.ai.model.implementation.web.response.AiModelResponse;

public final class AiModelAssembler {
    private AiModelAssembler() {}

    public static AiModelBO toBO(AiModelRequest request) {
        return MapstructUtils.convert(request, AiModelBO.class);
    }

    public static AiModelResponse toResponse(AiModelBO bo) {
        return MapstructUtils.convert(bo, AiModelResponse.class);
    }
}
