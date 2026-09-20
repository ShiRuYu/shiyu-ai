package com.shiyu.ai.conversation.implementation.domain.chat.model;

import java.time.Instant;

/**
 * 封装 Persona Asset 相关的不可变数据及其字段约束。
 */
public record PersonaAsset(
        String id,
        long tenantId,
        long ownerUserId,
        Persona persona,
        Instant createdAt,
        Instant updatedAt) {
    public PersonaAsset {
        if (persona == null) throw new IllegalArgumentException("persona is required");
    }
}
