package com.shiyu.ai.education.dto;

/**
 * 知识点推荐响应
 *
 * @param knowledgeId   知识点 ID
 * @param knowledgeName 知识点名称
 * @param mastery       当前掌握度 (0-100)
 * @param recommendType 推荐类型: WEAK_POINT / RECENT / PREREQUISITE
 * @param reason        推荐理由
 * @param score         推荐评分 (0-100)，越高越推荐
 */
public record KnowledgeRecommendResponse(
        Long knowledgeId,
        String knowledgeName,
        Double mastery,
        String recommendType,
        String reason,
        Integer score
) {}
