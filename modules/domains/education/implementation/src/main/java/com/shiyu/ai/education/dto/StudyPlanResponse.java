package com.shiyu.ai.education.dto;

import java.util.List;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.education.domain.model.StudyPlanBO;

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
        List<DailyTaskResponse> items
) {}
