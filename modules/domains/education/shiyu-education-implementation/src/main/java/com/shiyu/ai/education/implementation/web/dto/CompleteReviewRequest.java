package com.shiyu.ai.education.implementation.web.dto;

public record CompleteReviewRequest(
        Long studentId,
        Double resultScore
) {}

