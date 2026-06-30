package com.shiyu.ai.education.dto;

public record ReviewTaskResponse(
        Long id,
        Long studentId,
        Long knowledgeId,
        String knowledgeName,
        Integer reviewRound,
        String reviewDate,
        String status,
        Double previousMastery
) {}
