package com.shiyu.ai.conversation.chat;

import java.time.Instant;

public record PersonaAsset(String id, long tenantId, long ownerUserId, Persona persona,
                           Instant createdAt, Instant updatedAt) {
    public PersonaAsset {
        if (persona == null) throw new IllegalArgumentException("persona is required");
    }
}
