package com.shiyu.ai.education.implementation.web.dto;

import java.time.LocalDate;

public record GeneratePlanRequest(
        Long studentId, Long targetKnowledgeId, LocalDate startDate, LocalDate endDate) {}
