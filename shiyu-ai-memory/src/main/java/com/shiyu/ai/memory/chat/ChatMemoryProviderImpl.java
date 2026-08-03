package com.shiyu.ai.memory.chat;

import com.shiyu.ai.memory.domain.model.ConversationMessageBO;
import com.shiyu.ai.memory.port.repository.ConversationMessageRepository;
import com.shiyu.ai.model.chat.ChatMemoryProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatMemoryProvider 实现 — 基于 ConversationMessageRepository 持久化对话记忆
 * <p>
 * 存储到 {db}.conversation_message 表，按 sessionId 查询最近 N 条消息。
 */
@Slf4j
@Component
public class ChatMemoryProviderImpl implements ChatMemoryProvider {

    private static final int MAX_HISTORY = 20;

    private final ConversationMessageRepository conversationMessageRepository;

    public ChatMemoryProviderImpl(ConversationMessageRepository conversationMessageRepository) {
        this.conversationMessageRepository = conversationMessageRepository;
    }

    @Override
    public List<ChatMessage> loadMemory(String sessionId) {
        List<ConversationMessageBO> boList = conversationMessageRepository.selectRecentBySession(sessionId, MAX_HISTORY);
        // selectRecentBySession 按 create_time DESC 排序，反转成时间正序
        java.util.Collections.reverse(boList);
        return boList.stream()
                .map(bo -> new ChatMessage(bo.getRole(), bo.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    public void saveMemory(String sessionId, List<ChatMessage> messages) {
        for (ChatMessage msg : messages) {
            ConversationMessageBO bo = new ConversationMessageBO();
            bo.setSessionId(sessionId);
            bo.setRole(msg.role());
            bo.setContent(msg.content());
            conversationMessageRepository.insert(bo);
        }
    }
}
