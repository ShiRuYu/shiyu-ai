package com.shiyu.ai.usage.service;

import com.shiyu.ai.usage.model.TokenUsageRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Token 用量数据访问
 */
@Slf4j
public class TokenUsageRepository {

    private final JdbcTemplate jdbcTemplate;

    public TokenUsageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
