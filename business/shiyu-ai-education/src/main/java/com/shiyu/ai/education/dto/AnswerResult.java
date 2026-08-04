package com.shiyu.ai.education.dto;

public record AnswerResult(
        boolean correct,
        String correctAnswer,
        String analysis
) {}
