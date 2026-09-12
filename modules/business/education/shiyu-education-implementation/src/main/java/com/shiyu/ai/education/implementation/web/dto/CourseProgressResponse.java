package com.shiyu.ai.education.implementation.web.dto;

/**
 * {@code CourseProgressResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param courseId 课程标识，表示该记录组件承载的数据。
 * @param courseName courseName 属性，表示该记录组件承载的数据。
 * @param completedSections completedSections 属性，表示该记录组件承载的数据。
 * @param totalSections totalSections 属性，表示该记录组件承载的数据。
 * @param progress progress 属性，表示该记录组件承载的数据。
 */
public record CourseProgressResponse(
        Long courseId,
        String courseName,
        Integer completedSections,
        Integer totalSections,
        Double progress) {}
