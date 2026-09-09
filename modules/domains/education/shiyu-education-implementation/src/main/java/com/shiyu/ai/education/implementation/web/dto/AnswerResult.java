package com.shiyu.ai.education.implementation.web.dto;

public record AnswerResult(
        boolean correct,
        String correctAnswer,
        String analysis
) {}

