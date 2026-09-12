package com.shiyu.ai.model.implementation.infrastructure.chat;

import com.shiyu.ai.model.contract.model.ChatResponse;

import java.util.List;

/**
 * ModelStreamEvent 领域事件，描述模型领域相关业务状态变化。
 * @param type 类型，表示该记录组件承载的数据。
 * @param blockIndex blockIndex 属性，表示该记录组件承载的数据。
 * @param text text 属性，表示该记录组件承载的数据。
 * @param reasoning reasoning 属性，表示该记录组件承载的数据。
 * @param toolCallId toolCallId 属性，表示该记录组件承载的数据。
 * @param toolName toolName 属性，表示该记录组件承载的数据。
 * @param toolArguments toolArguments 属性，表示该记录组件承载的数据。
 * @param promptTokens promptTokens 属性，表示该记录组件承载的数据。
 * @param completionTokens completionTokens 属性，表示该记录组件承载的数据。
 * @param totalTokens totalTokens 属性，表示该记录组件承载的数据。
 * @param cacheReadTokens cacheReadTokens 属性，表示该记录组件承载的数据。
 * @param cacheWriteTokens cacheWriteTokens 属性，表示该记录组件承载的数据。
 * @param reasoningTokens reasoningTokens 属性，表示该记录组件承载的数据。
 * @param finishReason finishReason 属性，表示该记录组件承载的数据。
 * @param providerRequestId providerRequestId 属性，表示该记录组件承载的数据。
 * @param toolCalls toolCalls 属性，表示该记录组件承载的数据。
 */
public record ModelStreamEvent(
        Type type,
        int blockIndex,
        String text,
        String reasoning,
        String toolCallId,
        String toolName,
        String toolArguments,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        Integer cacheReadTokens,
        Integer cacheWriteTokens,
        Integer reasoningTokens,
        String finishReason,
        String providerRequestId,
        List<ChatResponse.ToolCall> toolCalls) {
    /**
     * {@code Type} 表示模型模块中的一组受控业务状态或分类。
     */
    public enum Type {
        BLOCK_STARTED,
        TEXT_DELTA,
        REASONING_DELTA,
        TOOL_CALL_DELTA,
        BLOCK_COMPLETED,
        USAGE,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public ModelStreamEvent {
        if (type == null) throw new IllegalArgumentException("stream event type is required");
        text = text == null ? "" : text;
        reasoning = reasoning == null ? "" : reasoning;
        toolArguments = toolArguments == null ? "" : toolArguments;
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public static ModelStreamEvent text(int blockIndex, String text, String providerRequestId) {
        return new ModelStreamEvent(
                Type.TEXT_DELTA,
                blockIndex,
                text,
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                providerRequestId,
                List.of());
    }
}
