package com.shiyu.ai.education.implementation.web.dto;

import java.time.LocalDate;

/**
 * 封装 Generate Plan 相关的不可变数据及其字段约束。
 */
public record GeneratePlanRequest(
        Long studentId, Long targetKnowledgeId, LocalDate startDate, LocalDate endDate) {}
