package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 知识 Recommend 相关的不可变数据及其字段约束。
 */
public record KnowledgeRecommendResponse(
        Long knowledgeId,
        String knowledgeName,
        Double mastery,
        String recommendType,
        String reason,
        Integer score) {}
