package com.shiyu.ai.conversation;

import com.shiyu.ai.conversation.domain.ConversationMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Single deterministic message ordering used by preview and generation. */
public final class PromptAssembler {
    private PromptAssembler() { }

    public static List<ConversationMessage> assemble(List<ConversationMessage> messages, int maxMessages) {
        if (messages == null || messages.isEmpty()) return List.of();
        int limit = Math.max(1, Math.min(maxMessages, 1000));
        return messages.size() <= limit ? List.copyOf(messages) : List.copyOf(messages.subList(messages.size() - limit, messages.size()));
    }

    /**
     * Selects only the active leaf's ancestry.  A conversation stores all
     * branch candidates in one table, so sequence order alone is not a valid
     * prompt boundary once an edit or retry has created siblings.
     *
     * The repository returns messages in ascending order for this helper.
     */
    public static List<ConversationMessage> activePath(List<ConversationMessage> messages,
                                                        String activeLeafMessageId,
                                                        int maxMessages) {
        if (messages == null || messages.isEmpty()) return List.of();
        if (activeLeafMessageId == null || activeLeafMessageId.isBlank()) {
            return assemble(messages, maxMessages);
        }
        Map<String, ConversationMessage> byId = new HashMap<>();
        for (ConversationMessage message : messages) byId.put(message.id(), message);
        List<ConversationMessage> reverse = new ArrayList<>();
        String cursor = activeLeafMessageId;
        // A corrupt parent cycle must not make prompt assembly hang.
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
