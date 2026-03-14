package com.shiyu.ai.chat.lm;

import java.util.Map;

public record ModelConfig(
        PlatformEnum type,
        String model,
        String apiKey,
        String baseUrl,
        Map<String, Object> extra
) { }

