package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 Complete 复习 相关的不可变数据及其字段约束。
 */
public record CompleteReviewRequest(Long studentId, Double resultScore) {}
