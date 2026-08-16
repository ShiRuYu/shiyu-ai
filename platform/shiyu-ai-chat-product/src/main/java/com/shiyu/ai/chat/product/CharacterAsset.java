package com.shiyu.ai.chat.product;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

public record CharacterAsset(String id, long tenantId, long ownerUserId, CharacterCardV2 card,
                             String visibility, byte[] pngData, Instant createdAt, Instant updatedAt) {
    public CharacterAsset(String id, long tenantId, long ownerUserId, CharacterCardV2 card,
                          String visibility, Instant createdAt, Instant updatedAt) {
        this(id, tenantId, ownerUserId, card, visibility, null, createdAt, updatedAt);
    }
    public CharacterAsset {
        if (card == null) throw new IllegalArgumentException("character card is required");
        visibility = visibility == null || visibility.isBlank() ? "PRIVATE" : visibility.toUpperCase(Locale.ROOT);
        if (!List.of("PRIVATE", "PUBLIC", "TENANT").contains(visibility)) throw new IllegalArgumentException("unsupported character visibility");
        pngData = pngData == null ? null : pngData.clone();
    }
    @Override public byte[] pngData() { return pngData == null ? null : pngData.clone(); }
}
