package com.shiyu.ai.dal.mapper.agent;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.agent.TokenUsageDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface TokenUsageMapper extends BaseMapperFlex<TokenUsageDO> {

    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as call_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM token_usage " +
            "WHERE create_time >= DATEADD('DAY', -#{days}, CURRENT_TIMESTAMP) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> aggregateByDay(@Param("days") int days);

    @Select("SELECT YEARWEEK(create_time) as week, " +
            "COUNT(*) as call_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM token_usage " +
            "WHERE create_time >= DATEADD('WEEK', -#{weeks}, CURRENT_TIMESTAMP) " +
            "GROUP BY YEARWEEK(create_time) " +
            "ORDER BY week DESC")
    List<Map<String, Object>> aggregateByWeek(@Param("weeks") int weeks);

    @Select("SELECT FORMATDATETIME(create_time, 'YYYY-MM') as month, " +
            "COUNT(*) as call_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM token_usage " +
            "WHERE create_time >= DATEADD('MONTH', -#{months}, CURRENT_TIMESTAMP) " +
            "GROUP BY FORMATDATETIME(create_time, 'YYYY-MM') " +
            "ORDER BY month DESC")
    List<Map<String, Object>> aggregateByMonth(@Param("months") int months);

    @Select("SELECT platform, model, " +
            "COUNT(*) as call_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM token_usage " +
            "GROUP BY platform, model " +
            "ORDER BY total_tokens DESC")
    List<Map<String, Object>> aggregateByModel();

    @Select("SELECT " +
            "COUNT(*) as total_calls, " +
            "COALESCE(SUM(total_tokens), 0) as total_tokens, " +
            "COALESCE(SUM(cost), 0) as total_cost, " +
            "COALESCE(AVG(latency_ms), 0) as avg_latency_ms, " +
            "COUNT(DISTINCT platform) as platform_count, " +
            "COUNT(DISTINCT model) as model_count " +
            "FROM token_usage")
    Map<String, Object> getOverview();
}
