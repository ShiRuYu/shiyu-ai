package com.shiyu.ai.education.implementation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Persisted result visible only to the student who owns the attempt. */
public record ExamResultResponse(
        Long attemptId,
        Long examId,
        String status,
        BigDecimal score,
        BigDecimal totalScore,
        Instant submittedAt,
        List<QuestionResult> questions) {
    /** Submitted answer, correct answer, and awarded score. */
    @Schema(name = "ExamQuestionResult")
    public record QuestionResult(
            Long questionId,
            String title,
            String answer,
            String correctAnswer,
            String analysis,
            BigDecimal score,
            BigDecimal maxScore) {}
}
