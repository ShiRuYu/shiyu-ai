package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/**
 * 封装 Hybrid Recommend 相关的不可变数据及其字段约束。
 */
public record HybridRecommendResponse(
        Long studentId,
        List<KnowledgeRecommendResponse> knowledgeTop,
        List<QuestionRecommendResponse> questionTop,
        List<ResourceRecommendResponse> resourceTop,
        List<QuestionRecommendResponse> reviewTop,
        String overallAdvice,
        Long generateTime) {}
