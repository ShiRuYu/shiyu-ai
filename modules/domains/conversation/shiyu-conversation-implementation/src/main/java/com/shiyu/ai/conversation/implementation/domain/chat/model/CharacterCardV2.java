package com.shiyu.ai.conversation.implementation.domain.chat.model;

import java.util.List;
import java.util.Map;

/**
 * 封装 Character Card V 2 相关的不可变数据及其字段约束。
 */
public record CharacterCardV2(
        String spec,
        String name,
        String description,
        String scenario,
        String firstMessage,
        List<String> exampleDialogues,
        String systemPrompt,
        Map<String, Object> extensions,
        int version) {
    public CharacterCardV2 {
        spec = spec == null ? "chara_card_v2" : spec;
        version = version <= 0 ? 2 : version;
        exampleDialogues = exampleDialogues == null ? List.of() : List.copyOf(exampleDialogues);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }
}
