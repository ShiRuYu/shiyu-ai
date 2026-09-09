package com.shiyu.ai.education.implementation.web.dto;

public record AbilityResponse(
        Long id,
        Long studentId,
        Long knowledgeId,
        String knowledgeName,
        Double overallMastery
) {}

