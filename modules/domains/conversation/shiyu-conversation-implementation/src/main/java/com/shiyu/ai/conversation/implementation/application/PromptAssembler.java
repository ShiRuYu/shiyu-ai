package com.shiyu.ai.conversation.implementation.application;

import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将 提示词 在不同层之间进行适配、转换或组装。
 */
public final class PromptAssembler {
    private PromptAssembler() {}

    /**
     * 构建或转换 提示词 相关业务数据，并返回处理结果。
     *
     * @param messages 用于完成本次业务处理的 messages 参数。
     * @param maxMessages 用于完成本次业务处理的 maxMessages 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 提示词 相关业务数据，并返回处理结果。
     *
     * @param messages 用于完成本次业务处理的 messages 参数。
     * @param activeLeafMessageId 用于定位active Leaf 消息的标识。
     * @param maxMessages 用于完成本次业务处理的 maxMessages 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
