package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * 封装 Wrong 题目 相关的不可变数据及其字段约束。
 */
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
