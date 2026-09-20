package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 题目 Recommend 相关的不可变数据及其字段约束。
 */
public record QuestionRecommendResponse(
        Long questionId,
        String title,
        String type,
        Integer difficulty,
        Long knowledgeId,
        String knowledgeName,
        String recommendType,
        String reason,
        Integer score) {}
