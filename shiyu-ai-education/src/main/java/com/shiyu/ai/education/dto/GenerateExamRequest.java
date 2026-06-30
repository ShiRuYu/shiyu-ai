package com.shiyu.ai.education.dto;

import java.util.List;

public record GenerateExamRequest(
        String subjectCode,
        Integer grade,
        List<Long> knowledgeIds,
        Integer durationMin,
        String difficultyDistribution
) {}
