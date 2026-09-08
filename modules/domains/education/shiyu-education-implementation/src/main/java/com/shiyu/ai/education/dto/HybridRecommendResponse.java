package com.shiyu.ai.education.dto;

import java.util.List;

/**
 * 混合推荐响应 — 聚合所有推荐维度的总接口
 *
 * @param studentId        学生 ID
 * @param knowledgeTop    知识点推荐 (Top 5)
 * @param questionTop     题目推荐 (Top 10)
 * @param resourceTop     资源推荐 (Top 5)
 * @param reviewTop       复习推荐 (Top 5)
 * @param overallAdvice   综合学习建议 (LLM 生成)
 * @param generateTime    推荐生成时间戳
 */
public record HybridRecommendResponse(
        Long studentId,
        List<KnowledgeRecommendResponse> knowledgeTop,
        List<QuestionRecommendResponse> questionTop,
        List<ResourceRecommendResponse> resourceTop,
        List<QuestionRecommendResponse> reviewTop,
        String overallAdvice,
        Long generateTime
) {}
