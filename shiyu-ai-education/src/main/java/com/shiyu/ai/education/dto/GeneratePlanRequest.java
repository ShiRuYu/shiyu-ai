package com.shiyu.ai.education.dto;

import java.time.LocalDate;

public record GeneratePlanRequest(
        Long studentId,
        Long targetKnowledgeId,
        LocalDate startDate,
        LocalDate endDate
) {}
