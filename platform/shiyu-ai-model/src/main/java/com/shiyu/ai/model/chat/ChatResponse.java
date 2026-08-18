package com.shiyu.ai.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    /** Complete tool calls for non-streaming providers; streaming uses TOOL_CALL events. */
    private List<ToolCall> toolCalls;

    public record ToolCall(String id, String name, String arguments) { }
}
