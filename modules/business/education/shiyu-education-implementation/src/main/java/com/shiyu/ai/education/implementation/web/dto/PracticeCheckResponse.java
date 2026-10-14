package com.shiyu.ai.education.implementation.web.dto;

/** Immediate feedback after a student submits one practice answer. */
public record PracticeCheckResponse(Long questionId, boolean correct,
        String correctAnswer, String analysis) {}
