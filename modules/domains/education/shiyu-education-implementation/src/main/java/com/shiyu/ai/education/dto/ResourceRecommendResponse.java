package com.shiyu.ai.education.dto;

/**
 * 学习资源推荐响应
 *
 * @param resourceId    资源 ID
 * @param title         资源名称
 * @param type          资源类型 (VIDEO / DOCUMENT / ARTICLE / EXERCISE)
 * @param knowledgeId   关联知识点 ID
 * @param knowledgeName 知识点名称
 * @param recommendType 推荐类型: BEGINNER / WEAK_POINT_RESOURCE / CURRENT_LEARNING
 * @param reason        推荐理由
 * @param score         推荐评分 (0-100)，越高越推荐
 */
public record ResourceRecommendResponse(
        Long resourceId,
        String title,
        String type,
        Long knowledgeId,
        String knowledgeName,
        String recommendType,
        String reason,
        Integer score
) {}
