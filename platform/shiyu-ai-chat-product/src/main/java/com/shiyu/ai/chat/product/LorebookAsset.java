package com.shiyu.ai.chat.product;

import java.time.Instant;

public record LorebookAsset(String id, long tenantId, long ownerUserId, LorebookEntry entry,
                            Instant createdAt, Instant updatedAt) {
    public LorebookAsset {
        if (entry == null) throw new IllegalArgumentException("lorebook entry is required");
    }
}
