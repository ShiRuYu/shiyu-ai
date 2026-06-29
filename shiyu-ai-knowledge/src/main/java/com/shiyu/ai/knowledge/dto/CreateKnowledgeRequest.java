package com.shiyu.ai.knowledge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateKnowledgeRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String subjectCode,
        @Min(0) Integer grade,
        String gradeLevel,
        String description,
        @Min(1) @Max(4) Integer difficulty,
        Integer estimatedTime,
        String suitableAge
) {
}
