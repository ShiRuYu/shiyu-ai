package com.shiyu.ai.conversation.implementation.domain.chat.model;

import java.time.Instant;

/**
 * 封装 Lorebook Asset 相关的不可变数据及其字段约束。
 */
public record LorebookAsset(
        String id,
        long tenantId,
        long ownerUserId,
        LorebookEntry entry,
        Instant createdAt,
        Instant updatedAt) {
    public LorebookAsset {
        if (entry == null) throw new IllegalArgumentException("lorebook entry is required");
    }
}
