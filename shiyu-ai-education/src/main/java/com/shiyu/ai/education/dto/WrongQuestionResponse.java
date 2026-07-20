package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.education.bo.WrongQuestionBO;

@AutoMapper(target = WrongQuestionBO.class)
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
