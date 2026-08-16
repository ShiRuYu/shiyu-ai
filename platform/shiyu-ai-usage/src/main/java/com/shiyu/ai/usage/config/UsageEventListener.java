package com.shiyu.ai.usage.config;

import com.shiyu.ai.model.event.ModelCallEvent;
import com.shiyu.ai.model.event.EmbeddingCallEvent;
import com.shiyu.ai.usage.service.UsageRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用量事件监听器
 * <p>
 * 监听 LLM 调用事件（{@link ModelCallEvent}）和 Embedding 调用事件（{@link EmbeddingCallEvent}），
 * 自动记录全平台用量。
 * </p>
 */
@Slf4j
@Component
public class UsageEventListener {

    private final UsageRecordService usageRecordService;

    public UsageEventListener(UsageRecordService usageRecordService) {
        this.usageRecordService = usageRecordService;
    }

    /**
     * 监听 LLM 模型调用事件
     */
    @EventListener
    @Async
    public void onModelCall(ModelCallEvent event) {
        // ConversationUsageSink records GenerationRun calls after the durable
        // terminal transition; do not create a second billable ledger row here.
        if (event.getGenerationRunId() != null && !event.getGenerationRunId().isBlank()) return;
        usageRecordService.recordUsage(
            event.getPlatform(),
            event.getModel(),
            event.getPromptTokens(),
            event.getCompletionTokens(),
            event.getLatencyMs(),
            null,  // userId
            null   // sessionId
        );
    }

    /**
     * 监听 Embedding 向量化调用事件
     */
    @EventListener
    @Async
    public void onEmbeddingCall(EmbeddingCallEvent event) {
        usageRecordService.recordEmbedding(
            event.getModel(),
            event.getTextLength(),
            event.getEstimatedTokens(),
            event.getVectorCount(),
            event.getLatencyMs(),
            null,  // userId
            null   // sessionId
        );
    }
}
