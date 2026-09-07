package com.shiyu.ai.conversation.chat;

import java.util.List;

public record LorebookEntry(String id, List<String> keys, String content, int priority,
                            String insertionPosition, int tokenBudget, boolean enabled) {
    public LorebookEntry { keys = keys == null ? List.of() : List.copyOf(keys); }
}
