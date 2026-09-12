package com.shiyu.ai.governance.implementation.web.websocket;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.governance.implementation.usage.realtime.UsageRealtimePublisher;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * WebSocket 推送服务
 *
 * <p>封装用量数据的实时推送逻辑，供 {@code UsageEventListener} 或定时任务调用。
 */
@Slf4j
@Service
public class UsageWebSocketService implements UsageRealtimePublisher {

    /**
     * 处理器，表示当前对象中的对应属性。
     */
    private final UsageWebSocketHandler handler;

    /**
     * {@code UsageWebSocketService} 创建并初始化当前类型实例。
     *
     * @param handler 参数值，用于执行当前操作。
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
