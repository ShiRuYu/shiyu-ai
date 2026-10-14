package com.shiyu.ai.education.implementation.web.dto;

/** A current student's active wrong question without another student's data. */
public record WrongPracticeResponse(Long id, Long questionId, String title,
        String lastAnswer, int correctTimes) {}
