package com.shiyu.ai.education.dto;

public record AbilityResponse(
        Long id,
        Long studentId,
        Long knowledgeId,
        String knowledgeName,
        Double overallMastery
) {}
