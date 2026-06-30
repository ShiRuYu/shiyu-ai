package com.shiyu.ai.education.dto;

public record SubjectResponse(
        Long id,
        String code,
        String name,
        String gradeLevel,
        String icon,
        Integer sortOrder
) {}
