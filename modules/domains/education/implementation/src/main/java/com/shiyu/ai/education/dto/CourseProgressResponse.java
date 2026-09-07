package com.shiyu.ai.education.dto;

public record CourseProgressResponse(
        Long courseId,
        String courseName,
        Integer completedSections,
        Integer totalSections,
        Double progress
) {}
