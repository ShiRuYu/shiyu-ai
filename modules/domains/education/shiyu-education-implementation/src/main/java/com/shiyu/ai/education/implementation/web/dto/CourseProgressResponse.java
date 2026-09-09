package com.shiyu.ai.education.implementation.web.dto;

public record CourseProgressResponse(
        Long courseId,
        String courseName,
        Integer completedSections,
        Integer totalSections,
        Double progress
) {}

