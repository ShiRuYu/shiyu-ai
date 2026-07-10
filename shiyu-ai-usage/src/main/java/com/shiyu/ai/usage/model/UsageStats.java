package com.shiyu.ai.usage.model;

import java.time.LocalDateTime;

/**
 * 用量统计聚合
 */
public class UsageStats {

    private final long totalTokens;
    private final long totalCalls;
    private final double totalCost;
    private final double avgLatencyMs;
    private final long startTime;
    private final long endTime;

    public UsageStats(long totalTokens, long totalCalls, double totalCost,
                      double avgLatencyMs, long startTime, long endTime) {
        this.totalTokens = totalTokens;
        this.totalCalls = totalCalls;
        this.totalCost = totalCost;
        this.avgLatencyMs = avgLatencyMs;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getTotalTokens() { return totalTokens; }
    public long getTotalCalls() { return totalCalls; }
    public double getTotalCost() { return totalCost; }
    public double getAvgLatencyMs() { return avgLatencyMs; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
}
