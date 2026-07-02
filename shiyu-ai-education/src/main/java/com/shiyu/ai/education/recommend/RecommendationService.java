package com.shiyu.ai.education.recommend;

import java.util.List;
import java.util.Map;

/**
 * Recommendation 接口
 */

public interface RecommendationService {
    /**
     * Recommend Knowledge
     * @return 处理结果
     */
    List<Map<String, Object>> recommendKnowledge(Long studentId, int topK);
    /**
     * Recommend Questions
     * @return 处理结果
     */
    List<Map<String, Object>> recommendQuestions(Long studentId, int count);
    /**
     * Recommend Resources
     * @return 处理结果
     */
    List<Map<String, Object>> recommendResources(Long studentId, int topK);
}
