package com.shiyu.ai.usage.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token 用量记录
 */
public class TokenUsageRecord {

    private String id;
    private final String platform;
    private final String model;
    private final int promptTokens;
    private final int completionTokens;
    private final int totalTokens;
    private final long latencyMs;
    private final double cost;
    private final Long userId;
    private final String sessionId;
    private final LocalDateTime timestamp;

    public TokenUsageRecord(String platform, String model,
                            int promptTokens, int completionTokens,
                            long latencyMs, double cost,
                            Long userId, String sessionId) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.platform = platform;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
        this.latencyMs = latencyMs;
        this.cost = cost;
        this.userId = userId;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getPlatform() { return platform; }
    public String getModel() { return model; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public int getTotalTokens() { return totalTokens; }
    public long getLatencyMs() { return latencyMs; }
    public double getCost() { return cost; }
    public Long getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
