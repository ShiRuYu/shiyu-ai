package com.shiyu.ai.education.implementation.web.dto;

/** 一次学生练习提交的持久化结果，可幂等返回。 */
public record PracticeSubmissionResponse(
        Long recordId, Long questionId, boolean correct, String correctAnswer,
        String analysis) {}
