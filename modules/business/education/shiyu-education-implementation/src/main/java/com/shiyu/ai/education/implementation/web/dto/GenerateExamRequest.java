package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/**
 * 封装 Generate 考试 相关的不可变数据及其字段约束。
 */
public record GenerateExamRequest(
        String subjectCode,
        Integer grade,
        List<Long> knowledgeIds,
        Integer durationMin,
        String difficultyDistribution) {}
