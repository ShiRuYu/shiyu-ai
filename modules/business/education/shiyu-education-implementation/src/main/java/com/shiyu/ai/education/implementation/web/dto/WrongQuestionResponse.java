package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * {@code WrongQuestionResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param studentId 学生标识，表示该记录组件承载的数据。
 * @param questionId 题目标识，表示该记录组件承载的数据。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param questionTitle questionTitle 属性，表示该记录组件承载的数据。
 * @param studentAnswer studentAnswer 属性，表示该记录组件承载的数据。
 * @param correctAnswer correctAnswer 属性，表示该记录组件承载的数据。
 * @param correctTimes correctTimes 属性，表示该记录组件承载的数据。
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
