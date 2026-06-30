package com.shiyu.ai.education.dto;

public record QuestionResponse(
        Long id,
        String code,
        String type,
        String subjectCode,
        Integer grade,
        Integer difficulty,
        String abilityDimension,
        String title,
        String options,
        String answer,
        String analysis,
        String tags,
        Long usedCount
) {}
