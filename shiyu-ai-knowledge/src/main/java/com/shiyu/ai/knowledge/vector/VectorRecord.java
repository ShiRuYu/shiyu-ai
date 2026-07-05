package com.shiyu.ai.knowledge.vector;

import java.util.Map;

public record VectorRecord(
        String id,
        float[] vector,
        Map<String, Object> metadata
) {
}
