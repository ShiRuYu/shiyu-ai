package com.shiyu.ai.education.service;

import com.shiyu.ai.education.dto.*;

import java.util.List;

/**
 * 智能推荐服务接口
 * <p>
 * 提供四类推荐 + 混合推荐聚合：
 * <ul>
 *   <li>知识点推荐 — 基于能力差距 + 图谱依赖</li>
 *   <li>题目推荐 — 基于薄弱点 + 难度 + 能力维度</li>
 *   <li>资源推荐 — 基于学习进度 + 知识点关联</li>
 *   <li>复习推荐 — 基于遗忘曲线的到期/即将到期复习任务</li>
 *   <li>混合推荐 — 上述四类聚合 + 综合建议</li>
 * </ul>
 */
public interface RecommendationService {

    /**
     * 推荐薄弱知识点 — 按掌握度升序排列
     */
    List<KnowledgeRecommendResponse> recommendKnowledge(Long studentId, int topK);

    /**
     * 推荐题目 — 基于薄弱知识点 + 难度匹配 + 能力维度
     */
    List<QuestionRecommendResponse> recommendQuestions(Long studentId, int count);

    /**
     * 推荐学习资源 — 基于薄弱点 + 最近学习知识点
     */
    List<ResourceRecommendResponse> recommendResources(Long studentId, int topK);

    /**
     * 推荐复习任务 — 基于遗忘曲线，到期/即将到期复习项
     */
    List<QuestionRecommendResponse> recommendReviewTasks(Long studentId, int count);

    /**
     * 混合推荐 — 聚合以上四类 + 综合学习建议
     */
    HybridRecommendResponse hybridRecommend(Long studentId, String overallAdvice);

    /**
     * 获取学生薄弱知识点 ID 列表（掌握度 &lt; 60）
     */
    List<Long> getWeakKnowledgeIds(Long studentId);
}
