package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 Submit Answer 相关的不可变数据及其字段约束。
 */
public record SubmitAnswerRequest(Long studentId, String answer) {}
