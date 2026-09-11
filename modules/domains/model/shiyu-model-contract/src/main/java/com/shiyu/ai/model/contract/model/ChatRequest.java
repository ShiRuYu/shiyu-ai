package com.shiyu.ai.model.contract.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Provider-neutral request passed from conversation/agent code to a model adapter. */
@Data
@Builder
@AllArgsConstructor
public class ChatRequest {
    private String platform;
    private String model;
    private String modelRouteId;
    private long tenantId;
    private long userId;
    private String generationRunId;
    private List<ChatMessage> messages;
    private ChatType chatType;
    private Double temperature;
    private Integer maxOutputTokens;
    private String reasoningEffort;
    private List<ToolDefinition> tools;

    public ChatRequest() {
        this.messages = List.of();
        this.tools = List.of();
    }

    /** Replaces the message history with an immutable provider-neutral copy. */
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages == null ? List.of() : List.copyOf(messages);
    }

    /** Replaces tool definitions with an immutable copy suitable for adapter mapping. */
    public void setTools(List<ToolDefinition> tools) {
        this.tools = tools == null ? List.of() : List.copyOf(tools);
    }

    /** Function tool definition expressed as JSON Schema without vendor coupling. */
    public record ToolDefinition(String name, String description, String parametersJson) {}
}
