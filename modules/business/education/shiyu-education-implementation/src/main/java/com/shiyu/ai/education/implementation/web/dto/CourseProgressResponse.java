package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 课程 Progress 相关的不可变数据及其字段约束。
 */
public record CourseProgressResponse(
        Long courseId,
        String courseName,
        Integer completedSections,
        Integer totalSections,
        Double progress) {}
