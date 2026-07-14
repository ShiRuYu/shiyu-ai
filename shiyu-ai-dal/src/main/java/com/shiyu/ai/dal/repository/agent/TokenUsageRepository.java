package com.shiyu.ai.dal.repository.agent;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.agent.TokenUsageDO;
import com.shiyu.ai.dal.mapper.agent.TokenUsageMapper;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Token 用量数据访问
 * 使用 MyBatis-Flex 写入，JdbcTemplate 读取聚合报表
 */
@Component
public class TokenUsageRepository {

    @Resource
    private TokenUsageMapper tokenUsageMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 插入用量记录
     */
    public void insert(TokenUsageDO tokenUsageDO) {
        tokenUsageMapper.insertSelective(tokenUsageDO);
    }

    /**
     * 按天查询聚合用量
     */
    public List<Map<String, Object>> aggregateByDay(int days) {
        String sql = "SELECT DATE(create_time) as date, " +
            "COUNT(*) as call_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM token_usage " +
            "WHERE create_time >= DATEADD('DAY', -?, CURRENT_TIMESTAMP) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date DESC";
        return jdbcTemplate.queryForList(sql, days);
    }

    /**
     * 按周查询聚合用量
     */
    public List<Map<String, Object>> aggregateByWeek(int weeks) {
        String sql = "SELECT YEARWEEK(create_time) as week, " +
            "COUNT(*) as call_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM token_usage " +
            "WHERE create_time >= DATEADD('WEEK', -?, CURRENT_TIMESTAMP) " +
            "GROUP BY YEARWEEK(create_time) " +
            "ORDER BY week DESC";
        return jdbcTemplate.queryForList(sql, weeks);
    }

    /**
     * 按月查询聚合用量
     */
    public List<Map<String, Object>> aggregateByMonth(int months) {
        String sql = "SELECT FORMATDATETIME(create_time, 'YYYY-MM') as month, " +
            "COUNT(*) as call_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM token_usage " +
            "WHERE create_time >= DATEADD('MONTH', -?, CURRENT_TIMESTAMP) " +
            "GROUP BY FORMATDATETIME(create_time, 'YYYY-MM') " +
            "ORDER BY month DESC";
        return jdbcTemplate.queryForList(sql, months);
    }

    /**
     * 按模型/平台维度分组统计
     */
    public List<Map<String, Object>> aggregateByModel() {
        String sql = "SELECT platform, model, " +
            "COUNT(*) as call_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost) as total_cost, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM token_usage " +
            "GROUP BY platform, model " +
            "ORDER BY total_tokens DESC";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取总体概览统计
     */
    public Map<String, Object> getOverview() {
        String sql = "SELECT " +
            "COUNT(*) as total_calls, " +
            "COALESCE(SUM(total_tokens), 0) as total_tokens, " +
            "COALESCE(SUM(cost), 0) as total_cost, " +
            "COALESCE(AVG(latency_ms), 0) as avg_latency_ms, " +
            "COUNT(DISTINCT platform) as platform_count, " +
            "COUNT(DISTINCT model) as model_count " +
            "FROM token_usage";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return results.isEmpty() ? Map.of() : results.get(0);
    }
}
