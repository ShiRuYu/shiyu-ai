package com.shiyu.ai.conversation.implementation.domain.chat;

import java.util.List;
import java.util.Map;

/**
 * {@code CharacterCardV2} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param spec spec 属性，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param description 描述，表示该记录组件承载的数据。
 * @param scenario scenario 属性，表示该记录组件承载的数据。
 * @param firstMessage firstMessage 属性，表示该记录组件承载的数据。
 * @param exampleDialogues exampleDialogues 属性，表示该记录组件承载的数据。
 * @param systemPrompt systemPrompt 属性，表示该记录组件承载的数据。
 * @param extensions extensions 属性，表示该记录组件承载的数据。
 * @param version version 属性，表示该记录组件承载的数据。
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
