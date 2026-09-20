package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 Answer 相关的不可变数据及其字段约束。
 */
public record AnswerResult(boolean correct, String correctAnswer, String analysis) {}
