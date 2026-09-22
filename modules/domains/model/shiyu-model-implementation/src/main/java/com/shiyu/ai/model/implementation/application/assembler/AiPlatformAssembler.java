package com.shiyu.ai.model.implementation.application.assembler;

import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.web.request.AiPlatformRequest;
import com.shiyu.ai.model.implementation.web.response.AiPlatformResponse;

/**
 * 将 AI 平台 在不同层之间进行适配、转换或组装。
 */
public final class AiPlatformAssembler {
    private AiPlatformAssembler() {}

    /**
     * 构建或转换 AI 平台 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 AI 平台 相关操作生成的结果数据。
     */
    public static AiPlatformBO toBO(AiPlatformRequest request) {
        return MapstructUtils.convert(request, AiPlatformBO.class);
    }

    /**
     * 构建或转换 AI 平台 相关业务数据，并返回处理结果。
     *
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 AI 平台 相关操作生成的结果数据。
     */
    public static AiPlatformResponse toResponse(AiPlatformBO bo) {
        return MapstructUtils.convert(bo, AiPlatformResponse.class);
    }
}
