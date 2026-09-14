package com.shiyu.ai.governance.implementation.usage.port.repository;

/**
 * 平台用量统计仓储，只允许平台统计服务使用，不接受客户端租户范围参数。
 */
public interface PlatformUsageRepository {
    /** 查询全平台用量概览。 */
    java.util.Map<String, Object> getOverview();
    /** 按最近指定天数汇总全平台用量。 */
    java.util.List<java.util.Map<String, Object>> aggregateByDay(int days);
    /** 按最近指定周数汇总全平台用量。 */
    java.util.List<java.util.Map<String, Object>> aggregateByWeek(int weeks);
    /** 按最近指定月数汇总全平台用量。 */
    java.util.List<java.util.Map<String, Object>> aggregateByMonth(int months);
    /** 按模型汇总全平台用量。 */
    java.util.List<java.util.Map<String, Object>> aggregateByModel();
    /** 按最近指定天数汇总全平台 LLM 用量。 */
    java.util.List<java.util.Map<String, Object>> aggregateLlmByDay(int days);
    /** 按最近指定周数汇总全平台 LLM 用量。 */
    java.util.List<java.util.Map<String, Object>> aggregateLlmByWeek(int weeks);
    /** 按最近指定月数汇总全平台 LLM 用量。 */
    java.util.List<java.util.Map<String, Object>> aggregateLlmByMonth(int months);
    /** 查询全平台 Embedding 用量概览。 */
    java.util.Map<String, Object> getEmbeddingOverview();
}
