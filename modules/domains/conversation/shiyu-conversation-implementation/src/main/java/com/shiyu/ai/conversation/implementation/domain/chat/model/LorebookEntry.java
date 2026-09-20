package com.shiyu.ai.conversation.implementation.domain.chat.model;

import java.util.List;

/**
 * 封装 Lorebook 相关的不可变数据及其字段约束。
 */
public record LorebookEntry(
        String id,
        List<String> keys,
        String content,
        int priority,
        String insertionPosition,
        int tokenBudget,
        boolean enabled) {
    public LorebookEntry {
        keys = keys == null ? List.of() : List.copyOf(keys);
    }
}
