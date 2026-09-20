package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 资源 Recommend 相关的不可变数据及其字段约束。
 */
public record ResourceRecommendResponse(
        Long resourceId,
        String title,
        String type,
        Long knowledgeId,
        String knowledgeName,
        String recommendType,
        String reason,
        Integer score) {}
