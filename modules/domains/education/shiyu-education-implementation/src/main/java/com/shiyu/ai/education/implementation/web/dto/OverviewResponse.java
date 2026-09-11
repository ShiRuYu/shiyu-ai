package com.shiyu.ai.education.implementation.web.dto;

public record OverviewResponse(
        Integer totalStudyDays,
        Integer totalKnowledge,
        Integer masteredKnowledge,
        Integer totalQuestions,
        Double accuracy,
        Double weeklyHours,
        Integer streakDays) {}
