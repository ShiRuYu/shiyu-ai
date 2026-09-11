package com.shiyu.ai.model.implementation.application.assembler;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.web.request.AiPlatformRequest;
import com.shiyu.ai.model.implementation.web.response.AiPlatformResponse;

public final class AiPlatformAssembler {
    private AiPlatformAssembler() {}

    public static AiPlatformBO toBO(AiPlatformRequest request) {
        return MapstructUtils.convert(request, AiPlatformBO.class);
    }

    public static AiPlatformResponse toResponse(AiPlatformBO bo) {
        return MapstructUtils.convert(bo, AiPlatformResponse.class);
    }
}
