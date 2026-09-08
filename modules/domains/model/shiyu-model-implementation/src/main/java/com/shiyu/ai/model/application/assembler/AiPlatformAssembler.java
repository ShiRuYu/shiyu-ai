package com.shiyu.ai.model.application.assembler;

import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;

public final class AiPlatformAssembler {
    private AiPlatformAssembler() {}
    public static AiPlatformBO toBO(AiPlatformRequest request) { return MapstructUtils.convert(request, AiPlatformBO.class); }
    public static AiPlatformResponse toResponse(AiPlatformBO bo) { return MapstructUtils.convert(bo, AiPlatformResponse.class); }
}
