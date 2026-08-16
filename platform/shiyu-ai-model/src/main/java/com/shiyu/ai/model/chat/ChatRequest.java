package com.shiyu.ai.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import com.shiyu.ai.model.ChatType;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ChatRequest {

    private String platform;

    private String model;

    /** Durable Conversation GenerationRun id, when this call is billed from the run ledger. */
    private String generationRunId;

    private List<ChatMessage> messages;

    private ChatType chatType;

    private Double temperature;

    private Integer maxOutputTokens;

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
