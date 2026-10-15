package com.shiyu.ai.education.implementation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** 已保存的考试结果，仅对应作答记录所属学生可见。 */
public record ExamResultResponse(
        Long attemptId,
        Long examId,
        String status,
        BigDecimal score,
        BigDecimal totalScore,
        Instant submittedAt,
        List<QuestionResult> questions) {
    /** 提交的答案、正确答案及获得的分数。 */
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
