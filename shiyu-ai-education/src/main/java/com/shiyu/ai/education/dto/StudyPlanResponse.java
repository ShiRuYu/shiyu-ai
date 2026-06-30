package com.shiyu.ai.education.dto;

import java.util.List;

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
