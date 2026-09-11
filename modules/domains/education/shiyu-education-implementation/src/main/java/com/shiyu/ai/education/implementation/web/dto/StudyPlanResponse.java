package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.StudyPlanBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.util.List;

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
