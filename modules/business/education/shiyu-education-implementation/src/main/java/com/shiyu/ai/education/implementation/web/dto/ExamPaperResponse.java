package com.shiyu.ai.education.implementation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** A student-visible exam paper; answer keys and explanations are deliberately absent. */
public record ExamPaperResponse(
        Long examId,
        Long attemptId,
        String name,
        String subjectCode,
        Integer grade,
        Integer durationMin,
        Integer totalScore,
        Instant deadlineAt,
        List<Section> sections) {

    /** Ordered section without answer keys. */
    @Schema(name = "ExamPaperSection")
    public record Section(Long id, String name, Integer orderNo, List<Question> questions) {}

    /** Student-visible question without grading fields. */
    @Schema(name = "ExamPaperQuestion")
    public record Question(
            Long id,
            Long questionId,
            String title,
            String type,
            BigDecimal score,
            List<Option> options) {}

    /** Choice value and display label. */
    @Schema(name = "ExamPaperOption")
    public record Option(String value, String label) {}
}
