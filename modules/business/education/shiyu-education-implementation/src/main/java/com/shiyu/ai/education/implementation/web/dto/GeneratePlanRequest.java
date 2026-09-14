package com.shiyu.ai.education.implementation.web.dto;

import java.time.LocalDate;

/**
 * {@code GeneratePlanRequest} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param studentId 学生标识，表示该记录组件承载的数据。
 * @param targetKnowledgeId targetKnowledgeId 属性，表示该记录组件承载的数据。
 * @param startDate startDate 属性，表示该记录组件承载的数据。
 * @param endDate endDate 属性，表示该记录组件承载的数据。
 */
public record GeneratePlanRequest(
        Long studentId, Long targetKnowledgeId, LocalDate startDate, LocalDate endDate) {}
