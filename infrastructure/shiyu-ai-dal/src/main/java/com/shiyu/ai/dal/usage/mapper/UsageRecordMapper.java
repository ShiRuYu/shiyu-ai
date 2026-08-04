package com.shiyu.ai.dal.usage.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.usage.dataobject.UsageRecordDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UsageRecordMapper extends BaseMapperFlex<UsageRecordDO> {

    /** 按日聚合（按类型分组） */
    @Select("SELECT DATE(create_time) as date, " +
            "usage_type, " +
            "COUNT(*) as call_count, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE create_time >= DATEADD('DAY', -#{days}, CURRENT_TIMESTAMP) " +
            "GROUP BY DATE(create_time), usage_type " +
            "ORDER BY date DESC")
    List<Map<String, Object>> aggregateByDay(@Param("days") int days);

    /** 按周聚合 */
    @Select("SELECT YEARWEEK(create_time) as week, " +
            "usage_type, " +
            "COUNT(*) as call_count, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE create_time >= DATEADD('WEEK', -#{weeks}, CURRENT_TIMESTAMP) " +
            "GROUP BY YEARWEEK(create_time), usage_type " +
            "ORDER BY week DESC")
    List<Map<String, Object>> aggregateByWeek(@Param("weeks") int weeks);

    /** 按月聚合 */
    @Select("SELECT FORMATDATETIME(create_time, 'YYYY-MM') as month, " +
            "usage_type, " +
            "COUNT(*) as call_count, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE create_time >= DATEADD('MONTH', -#{months}, CURRENT_TIMESTAMP) " +
            "GROUP BY FORMATDATETIME(create_time, 'YYYY-MM'), usage_type " +
            "ORDER BY month DESC")
    List<Map<String, Object>> aggregateByMonth(@Param("months") int months);

    /** 全局概览 */
    @Select("SELECT " +
            "COUNT(*) as total_calls, " +
            "AVG(latency_ms) as avg_latency_ms, " +
            "COUNT(DISTINCT usage_type) as type_count " +
            "FROM agent_usage_record")
    Map<String, Object> getOverview();

    /** LLM 按模型聚合 */
    @Select("SELECT " +
            "JSON_EXTRACT(ext_info, '$.platform') as platform, " +
            "JSON_EXTRACT(ext_info, '$.model') as model, " +
            "COUNT(*) as call_count, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.totalTokens') AS NUMERIC)), 0) as total_tokens, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.cost') AS NUMERIC)), 0) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE usage_type = 'LLM' " +
            "GROUP BY JSON_EXTRACT(ext_info, '$.platform'), JSON_EXTRACT(ext_info, '$.model') " +
            "ORDER BY total_tokens DESC")
    List<Map<String, Object>> aggregateByModel();

    /** LLM 按日聚合（含 token/cost） */
    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as call_count, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.totalTokens') AS NUMERIC)), 0) as total_tokens, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.cost') AS NUMERIC)), 0) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE usage_type = 'LLM' AND create_time >= DATEADD('DAY', -#{days}, CURRENT_TIMESTAMP) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> aggregateLlmByDay(@Param("days") int days);

    /** LLM 按周聚合（含 token/cost） */
    @Select("SELECT YEARWEEK(create_time) as week, " +
            "COUNT(*) as call_count, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.totalTokens') AS NUMERIC)), 0) as total_tokens, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.cost') AS NUMERIC)), 0) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE usage_type = 'LLM' AND create_time >= DATEADD('WEEK', -#{weeks}, CURRENT_TIMESTAMP) " +
            "GROUP BY YEARWEEK(create_time) " +
            "ORDER BY week DESC")
    List<Map<String, Object>> aggregateLlmByWeek(@Param("weeks") int weeks);

    /** LLM 按月聚合（含 token/cost） */
    @Select("SELECT FORMATDATETIME(create_time, 'YYYY-MM') as month, " +
            "COUNT(*) as call_count, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.totalTokens') AS NUMERIC)), 0) as total_tokens, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.cost') AS NUMERIC)), 0) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE usage_type = 'LLM' AND create_time >= DATEADD('MONTH', -#{months}, CURRENT_TIMESTAMP) " +
            "GROUP BY FORMATDATETIME(create_time, 'YYYY-MM') " +
            "ORDER BY month DESC")
    List<Map<String, Object>> aggregateLlmByMonth(@Param("months") int months);

    /** Embedding 概览 */
    @Select("SELECT " +
            "COUNT(*) as total_calls, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.estimatedTokens') AS NUMERIC)), 0) as total_estimated_tokens, " +
            "COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.vectorCount') AS NUMERIC)), 0) as total_vectors, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record WHERE usage_type = 'EMBEDDING'")
    Map<String, Object> getEmbeddingOverview();

    /** 查询指定租户当天的 LLM Token 总消耗 */
    @Select("SELECT COALESCE(SUM(CAST(JSON_EXTRACT(ext_info, '$.totalTokens') AS NUMERIC)), 0) " +
            "FROM agent_usage_record WHERE usage_type = 'LLM' " +
            "AND user_id IN (SELECT id FROM auth_user WHERE tenant_id = #{tenantId}) " +
            "AND create_time >= CURRENT_DATE")
    Long sumLlmTodayTokensByTenantId(@Param("tenantId") Long tenantId);
}
