package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.StudyPlanBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.util.List;

/**
 * 封装 Study Plan 相关的不可变数据及其字段约束。
 */
@AutoMapper(target = StudyPlanBO.class)
public record StudyPlanResponse(
        Long id,
        Long studentId,
        String name,
        String startDate,
        String endDate,
        Integer status,
        String statusDesc,
        Integer totalItems,
        Integer completedItems,
        List<DailyTaskResponse> items) {}
