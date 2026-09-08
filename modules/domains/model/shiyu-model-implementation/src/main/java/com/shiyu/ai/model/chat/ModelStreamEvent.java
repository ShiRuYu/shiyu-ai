package com.shiyu.ai.model.chat;

import java.util.List;

/** Provider-neutral stream vocabulary. Adapters must preserve block ordering and tool indexes. */
public record ModelStreamEvent(Type type, int blockIndex, String text, String reasoning,
                               String toolCallId, String toolName, String toolArguments,
                               Integer promptTokens, Integer completionTokens, Integer totalTokens,
                               Integer cacheReadTokens, Integer cacheWriteTokens, Integer reasoningTokens,
                               String finishReason, String providerRequestId, List<ChatResponse.ToolCall> toolCalls) {
    public enum Type {
        BLOCK_STARTED, TEXT_DELTA, REASONING_DELTA, TOOL_CALL_DELTA, BLOCK_COMPLETED,
        USAGE, COMPLETED, FAILED, CANCELLED
    }

    public ModelStreamEvent {
        if (type == null) throw new IllegalArgumentException("stream event type is required");
        text = text == null ? "" : text;
        reasoning = reasoning == null ? "" : reasoning;
        toolArguments = toolArguments == null ? "" : toolArguments;
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public static ModelStreamEvent text(int blockIndex, String text, String providerRequestId) {
        return new ModelStreamEvent(Type.TEXT_DELTA, blockIndex, text, "", null, null, null,
                null, null, null, null, null, null, null, providerRequestId, List.of());
    }
}
