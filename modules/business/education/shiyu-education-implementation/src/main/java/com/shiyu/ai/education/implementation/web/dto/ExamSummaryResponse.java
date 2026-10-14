package com.shiyu.ai.education.implementation.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Student-visible exam details and the authenticated student's own attempt state. */
public record ExamSummaryResponse(
        Long examId,
        String name,
        String type,
        String subjectCode,
        Integer grade,
        Integer durationMin,
        Integer totalScore,
        Long attemptId,
        String attemptStatus,
        Instant deadlineAt,
        BigDecimal score) {}
