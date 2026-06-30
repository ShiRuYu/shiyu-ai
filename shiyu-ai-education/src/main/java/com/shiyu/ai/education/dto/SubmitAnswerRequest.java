package com.shiyu.ai.education.dto;

public record SubmitAnswerRequest(
        Long studentId,
        String answer
) {}
