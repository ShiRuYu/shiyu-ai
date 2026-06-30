package com.shiyu.ai.education.dto;

public record CourseResponse(
        Long id,
        String name,
        String description,
        String subjectCode,
        Integer grade,
        Long textbookId,
        Long teacherId,
        String coverUrl,
        Integer totalHours,
        Integer status
) {}
