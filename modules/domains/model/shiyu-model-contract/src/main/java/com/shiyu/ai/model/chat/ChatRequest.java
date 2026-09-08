package com.shiyu.ai.model.chat;

import com.shiyu.ai.model.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages == null ? List.of() : List.copyOf(messages);
    }

    public void setTools(List<ToolDefinition> tools) {
        this.tools = tools == null ? List.of() : List.copyOf(tools);
    }

    public record ToolDefinition(String name, String description, String parametersJson) { }
}
