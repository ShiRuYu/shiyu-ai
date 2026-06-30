package com.shiyu.ai.education.dto;

public record ExamResponse(
        Long id,
        String name,
        String type,
        String subjectCode,
        Integer grade,
        Integer durationMin,
        Integer totalScore,
        Integer status
) {}
