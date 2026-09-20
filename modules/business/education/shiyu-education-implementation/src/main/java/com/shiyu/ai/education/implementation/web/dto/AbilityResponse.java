package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 Ability 相关的不可变数据及其字段约束。
 */
public record AbilityResponse(
        Long id, Long studentId, Long knowledgeId, String knowledgeName, Double overallMastery) {}
