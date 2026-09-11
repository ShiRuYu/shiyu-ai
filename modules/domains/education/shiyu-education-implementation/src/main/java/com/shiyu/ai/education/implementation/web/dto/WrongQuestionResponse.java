package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

@AutoMapper(target = WrongQuestionBO.class)
public record WrongQuestionResponse(
        Long id,
        Long studentId,
        Long questionId,
        Long knowledgeId,
        String questionTitle,
        String studentAnswer,
        String correctAnswer,
        Integer correctTimes) {}
