package com.shiyu.ai.knowledge.dto;

import java.util.List;

public record KnowledgeResponse(
        Long id,
        String code,
        String name,
        String subjectCode,
        Integer grade,
        String gradeLevel,
        String description,
        Integer difficulty,
        Integer estimatedTime,
        String suitableAge,
        List<Long> parentIds,
        List<Long> childIds
) {
}
