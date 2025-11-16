package com.shiyu.ai.chat.lm.model;

import java.util.Map;

public record ModelConfig(
        ModelEnum type,
        String model,
        String apiKey,
        String baseUrl,
        Map<String, Object> extra
) { }

