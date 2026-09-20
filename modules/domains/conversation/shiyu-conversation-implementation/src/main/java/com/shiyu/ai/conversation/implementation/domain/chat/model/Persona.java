package com.shiyu.ai.conversation.implementation.domain.chat.model;

import java.util.Map;

/**
 * 封装 Persona 相关的不可变数据及其字段约束。
 */
public record Persona(
        String id,
        long ownerUserId,
        String name,
        String identity,
        String tone,
        String visibility,
        Map<String, Object> attributes) {
    public Persona {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
