package com.shiyu.ai.conversation.implementation.application;

import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据会话上下文、角色卡和消息组装模型提示词。
 */
public final class PromptAssembler {
    private PromptAssembler() {}

    /**
     * {@code assemble} 执行当前类型定义的业务操作。
     *
     * @param messages 参数值，用于执行当前操作。
     * @param maxMessages 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static List<ConversationMessage> assemble(
            List<ConversationMessage> messages, int maxMessages) {
        if (messages == null || messages.isEmpty()) return List.of();
        int limit = Math.max(1, Math.min(maxMessages, 1000));
        return messages.size() <= limit
                ? List.copyOf(messages)
                : List.copyOf(messages.subList(messages.size() - limit, messages.size()));
    }

    /**
     * 处理启用路径。
     *
     * @param messages messages 参数。
     * @param activeLeafMessageId activeLeafMessageId 参数。
     * @param maxMessages maxMessages 参数。
     *
     * @return 结果列表。
     */
    public static List<ConversationMessage> activePath(
            List<ConversationMessage> messages, String activeLeafMessageId, int maxMessages) {
        if (messages == null || messages.isEmpty()) return List.of();
        if (activeLeafMessageId == null || activeLeafMessageId.isBlank()) {
            return assemble(messages, maxMessages);
        }
        Map<String, ConversationMessage> byId = new HashMap<>();
        for (ConversationMessage message : messages) byId.put(message.id(), message);
        List<ConversationMessage> reverse = new ArrayList<>();
        String cursor = activeLeafMessageId;
        int guard = Math.min(messages.size() + 1, 10_001);
        while (cursor != null && guard-- > 0) {
            ConversationMessage message = byId.get(cursor);
            if (message == null) break;
            reverse.add(message);
            cursor = message.parentMessageId();
        }
        if (reverse.isEmpty()) return assemble(messages, maxMessages);
        java.util.Collections.reverse(reverse);
        return assemble(reverse, maxMessages);
    }
}
