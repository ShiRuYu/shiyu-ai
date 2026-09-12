package com.shiyu.ai.education.implementation.web.dto;

/**
 * {@code AnswerResult} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param correct correct 属性，表示该记录组件承载的数据。
 * @param correctAnswer correctAnswer 属性，表示该记录组件承载的数据。
 * @param analysis analysis 属性，表示该记录组件承载的数据。
 */
public record AnswerResult(boolean correct, String correctAnswer, String analysis) {}
