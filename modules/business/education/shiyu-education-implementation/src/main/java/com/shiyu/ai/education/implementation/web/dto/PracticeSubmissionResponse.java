package com.shiyu.ai.education.implementation.web.dto;

/** Durable and idempotent result of one student practice submission. */
public record PracticeSubmissionResponse(
        Long recordId, Long questionId, boolean correct, String correctAnswer,
        String analysis) {}
