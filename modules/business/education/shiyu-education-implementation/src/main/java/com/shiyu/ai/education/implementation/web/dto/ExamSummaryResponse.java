package com.shiyu.ai.education.implementation.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** 学生可见的考试详情及已认证学生本人的作答状态。 */
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
