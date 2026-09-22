package com.shiyu.ai.governance.implementation.web.websocket;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.governance.implementation.usage.realtime.UsageRealtimePublisher;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 提供 用量 Web Socket 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class UsageWebSocketService implements UsageRealtimePublisher {

    /**
     * 处理器，表示当前对象中的对应属性。
     */
    private final UsageWebSocketHandler handler;

    /**
     * 执行 用量 Web Socket 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param handler 用于完成本次业务处理的 handler 参数。
     */
    public UsageWebSocketService(UsageWebSocketHandler handler) {
        this.handler = handler;
    }

    /** 推送单次 LLM 用量记录 */
    @Override
    public void publishUsageRecord(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            double cost) {
        if (handler.getActiveSessionCount() == 0) return;

        Map<String, Object> payload =
                Map.of(
                        "type", "USAGE_RECORD",
                        "timestamp", System.currentTimeMillis(),
                        "data",
                                Map.of(
                                        "platform", platform,
                                        "model", model,
                                        "promptTokens", promptTokens,
                                        "completionTokens", completionTokens,
                                        "totalTokens", promptTokens + completionTokens,
                                        "latencyMs", latencyMs,
                                        "cost", cost));
        broadcastSafely(payload);
        log.debug(
                "已推送 LLM 用量记录: platform={}, model={}, totalTokens={}",
                platform,
                model,
                promptTokens + completionTokens);
    }

    /** 推送单次 Embedding 用量记录 */
    @Override
    public void publishEmbeddingUsage(
            String model, int textLength, int estimatedTokens, int vectorCount, long latencyMs) {
        if (handler.getActiveSessionCount() == 0) return;

        Map<String, Object> payload =
                Map.of(
                        "type", "EMBEDDING_USAGE_RECORD",
                        "timestamp", System.currentTimeMillis(),
                        "data",
                                Map.of(
                                        "model", model,
                                        "textLength", textLength,
                                        "estimatedTokens", estimatedTokens,
                                        "vectorCount", vectorCount,
                                        "latencyMs", latencyMs));
        broadcastSafely(payload);
        log.debug(
                "已推送 Embedding 用量记录: model={}, vectors={}, tokens≈{}",
                model,
                vectorCount,
                estimatedTokens);
    }

    /** 推送上一次聚合统计（按日） */
    public void pushDailyAggregate(int days) {
        if (handler.getActiveSessionCount() == 0) return;

        Map<String, Object> payload =
                Map.of(
                        "type", "USAGE_DAILY_AGGREGATE",
                        "timestamp", System.currentTimeMillis(),
                        "data", Map.of("days", days, "date", LocalDate.now().toString()));
        broadcastSafely(payload);
    }

    /**
     * 安全广播用量事件。
     *
     * @param payload 事件载荷。
     */
    private void broadcastSafely(Map<String, Object> payload) {
        try {
            handler.broadcast(JSONUtils.toJsonString(payload));
        } catch (RuntimeException exception) {
            log.warn(
                    "WebSocket 用量推送失败: errorType={}, errorMessageLength={}",
                    exception.getClass().getSimpleName(),
                    exception.getMessage() == null ? 0 : exception.getMessage().length());
        }
    }
}
