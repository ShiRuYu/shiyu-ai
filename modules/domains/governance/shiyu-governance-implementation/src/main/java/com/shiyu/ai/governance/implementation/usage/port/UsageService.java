package com.shiyu.ai.governance.implementation.usage.port;

import java.util.List;
import java.util.Map;

/**
 * 提供 用量 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface UsageService {
    /**
     * 查询当前租户用量概览。
     *
     * @return 当前租户的用量概览。
     */
    Map<String, Object> overview();

    /**
     * 查询当前租户按日用量。
     *
     * @param days 查询的天数。
     * @return 按日聚合的用量。
     */
    List<Map<String, Object>> byDay(int days);

    /**
     * 查询当前租户按周用量。
     *
     * @param weeks 查询的周数。
     * @return 按周聚合的用量。
     */
    List<Map<String, Object>> byWeek(int weeks);

    /**
     * 查询当前租户按月用量。
     *
     * @param months 查询的月数。
     * @return 按月聚合的用量。
     */
    List<Map<String, Object>> byMonth(int months);

    /**
     * 查询当前租户按模型用量。
     *
     * @return 按模型聚合的用量。
     */
    List<Map<String, Object>> byModel();

    /**
     * 查询当前租户按日 LLM 用量。
     *
     * @param days 查询的天数。
     * @return 按日聚合的 LLM 用量。
     */
    List<Map<String, Object>> llmByDay(int days);

    /**
     * 查询当前租户按周 LLM 用量。
     *
     * @param weeks 查询的周数。
     * @return 按周聚合的 LLM 用量。
     */
    List<Map<String, Object>> llmByWeek(int weeks);

    /**
     * 查询当前租户按月 LLM 用量。
     *
     * @param months 查询的月数。
     * @return 按月聚合的 LLM 用量。
     */
    List<Map<String, Object>> llmByMonth(int months);

    /**
     * 查询当前租户 Embedding 用量概览。
     *
     * @return Embedding 用量概览。
     */
    Map<String, Object> embeddingOverview();
}
