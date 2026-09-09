package com.shiyu.ai.agent.implementation.runtime;

import com.shiyu.ai.agent.contract.runtime.*;

import java.util.Map;

public record AiAppPreview(String appId, String appVersionId, String status, String promptHash,
                           String model, Map<String, Object> configuration, boolean executable) {
    public AiAppPreview {
        configuration = configuration == null ? Map.of() : Map.copyOf(configuration);
    }
}
