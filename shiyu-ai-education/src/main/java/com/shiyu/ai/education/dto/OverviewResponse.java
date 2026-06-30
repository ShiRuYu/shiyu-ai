package com.shiyu.ai.education.dto;

public record OverviewResponse(
        Integer totalStudyDays,
        Integer totalKnowledge,
        Integer masteredKnowledge,
        Integer totalQuestions,
        Double accuracy,
        Double weeklyHours,
        Integer streakDays
) {}
