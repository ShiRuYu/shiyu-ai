package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.*;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 推荐 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface RecommendationService {

    /** 推荐薄弱知识点 — 按掌握度升序排列 */
    List<KnowledgeRecommendResponse> recommendKnowledge(
            ActorContext actor, Long studentId, int topK);

    /** 推荐题目 — 基于薄弱知识点 + 难度匹配 + 能力维度 */
    List<QuestionRecommendResponse> recommendQuestions(
            ActorContext actor, Long studentId, int count);

    /** 推荐学习资源 — 基于薄弱点 + 最近学习知识点 */
    List<ResourceRecommendResponse> recommendResources(
            ActorContext actor, Long studentId, int topK);

    /** 推荐复习任务 — 基于遗忘曲线，到期/即将到期复习项 */
    List<QuestionRecommendResponse> recommendReviewTasks(
            ActorContext actor, Long studentId, int count);

    /** 混合推荐 — 聚合以上四类 + 综合学习建议 */
    HybridRecommendResponse hybridRecommend(
            ActorContext actor, Long studentId, String overallAdvice);

    /** 获取学生薄弱知识点 ID 列表（掌握度 &lt; 60） */
    List<Long> getWeakKnowledgeIds(ActorContext actor, Long studentId);
}
