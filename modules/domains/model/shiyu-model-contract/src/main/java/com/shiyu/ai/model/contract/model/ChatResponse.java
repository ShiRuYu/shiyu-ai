package com.shiyu.ai.model.contract.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Provider-neutral completed or streaming chat event returned by the model boundary. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private boolean success;
    private String content;
    private String reasoningContent;
    private String platform;
    private String model;
    private String errorMessage;
    private String eventType;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private boolean estimatedUsage;
    private String toolCallId;
    private String toolName;
    private String toolArguments;
    private Integer blockIndex;
    private String finishReason;
    private String providerRequestId;
    private Integer cacheReadTokens;
    private Integer cacheWriteTokens;
    private Integer reasoningTokens;
    private List<ToolCall> toolCalls;

    /** Normalized function call emitted by a provider. */
    public record ToolCall(String id, String name, String arguments) {}
}
