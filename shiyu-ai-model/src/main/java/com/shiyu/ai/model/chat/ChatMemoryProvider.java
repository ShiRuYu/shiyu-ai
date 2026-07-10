package com.shiyu.ai.model.chat;

import java.util.List;

public interface ChatMemoryProvider {

    List<ChatMessage> loadMemory(String sessionId);

    void saveMemory(String sessionId, List<ChatMessage> messages);

    record ChatMessage(String role, String content) {}
}
