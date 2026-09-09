package com.shiyu.ai.conversation.implementation.domain.chat;

import java.util.Map;

public record Persona(String id, long ownerUserId, String name, String identity, String tone,
                      String visibility, Map<String, Object> attributes) {
    public Persona { attributes = attributes == null ? Map.of() : Map.copyOf(attributes); }
}
