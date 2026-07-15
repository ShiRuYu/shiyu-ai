package com.shiyu.ai.education.dto;

import java.util.List;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.bo.education.StudyPlanBO;

@AutoMapper(target = StudyPlanBO.class)
public record StudyPlanResponse(
        Long id,
        Long studentId,
        String name,
        String startDate,
        String endDate,
        String status,
        Integer totalItems,
        Integer completedItems,
        List<DailyTaskResponse> items
) {}
