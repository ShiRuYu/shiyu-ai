package com.shiyu.ai.education.dto;

public record CompleteReviewRequest(
        Long studentId,
        Double resultScore
) {}
