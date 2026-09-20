package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;

import java.util.Map;

/**
 * 封装 AI 应用 Preview 相关的不可变数据及其字段约束。
 */
public record AiAppPreview(
        String appId,
        String appVersionId,
        String status,
        String promptHash,
        String model,
        Map<String, Object> configuration,
        boolean executable) {
    public AiAppPreview {
        configuration = configuration == null ? Map.of() : Map.copyOf(configuration);
    }
}
