package com.shiyu.ai.knowledge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateKnowledgeRequest(
        String name,
        String description,
        @Min(1) @Max(4) Integer difficulty,
        Integer estimatedTime
) {
}
