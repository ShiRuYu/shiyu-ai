package com.shiyu.ai.education.dto;

public record WrongQuestionResponse(
        Long id,
        Long studentId,
        Long questionId,
        Long knowledgeId,
        String questionTitle,
        String studentAnswer,
        String correctAnswer,
        Integer correctTimes
) {}
