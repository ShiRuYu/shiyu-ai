package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 Overview 相关的不可变数据及其字段约束。
 */
public record OverviewResponse(
        Integer totalStudyDays,
        Integer totalKnowledge,
        Integer masteredKnowledge,
        Integer totalQuestions,
        Double accuracy,
        Double weeklyHours,
        Integer streakDays) {}
