package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

public record GenerateExamRequest(
        String subjectCode,
        Integer grade,
        List<Long> knowledgeIds,
        Integer durationMin,
        String difficultyDistribution) {}
