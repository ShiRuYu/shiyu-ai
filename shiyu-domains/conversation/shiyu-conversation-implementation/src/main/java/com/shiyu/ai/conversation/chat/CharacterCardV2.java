package com.shiyu.ai.conversation.chat;

import java.util.List;
import java.util.Map;

public record CharacterCardV2(String spec, String name, String description, String scenario,
                             String firstMessage, List<String> exampleDialogues, String systemPrompt,
                             Map<String, Object> extensions, int version) {
    public CharacterCardV2 {
        spec = spec == null ? "chara_card_v2" : spec;
        version = version <= 0 ? 2 : version;
        exampleDialogues = exampleDialogues == null ? List.of() : List.copyOf(exampleDialogues);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }
}
