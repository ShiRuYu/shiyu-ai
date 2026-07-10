package com.shiyu.ai.usage.service;

import com.shiyu.ai.usage.model.TokenUsageRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Token 用量数据访问
 */
@Slf4j
public class TokenUsageRepository {

    private final JdbcTemplate jdbcTemplate;

    public TokenUsageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        return jdbcTemplate.queryForList(sql).stream().findFirst().orElse(Map.of());
    }

    public void insert(TokenUsageRecord record) {
        jdbcTemplate.update(
            "INSERT INTO token_usage (id, platform, model, prompt_tokens, completion_tokens, " +
            "total_tokens, latency_ms, cost, user_id, session_id, create_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            record.getId(), record.getPlatform(), record.getModel(),
            record.getPromptTokens(), record.getCompletionTokens(),
            record.getTotalTokens(), record.getLatencyMs(), record.getCost(),
            record.getUserId(), record.getSessionId(),
            Timestamp.valueOf(record.getTimestamp())
        );
    }
}
