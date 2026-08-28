package com.shiyu.ai.model.application.assembler;

import com.shiyu.ai.model.api.request.AiModelRequest;
import com.shiyu.ai.model.api.response.AiModelResponse;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;

public final class AiModelAssembler {
    private AiModelAssembler() {}
    public static AiModelBO toBO(AiModelRequest request) { return MapstructUtils.convert(request, AiModelBO.class); }
    public static AiModelResponse toResponse(AiModelBO bo) { return MapstructUtils.convert(bo, AiModelResponse.class); }
}
