package com.shiyu.ai.conversation.implementation.domain.chat;

import java.util.Map;

/**
 * {@code Persona} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param identity identity 属性，表示该记录组件承载的数据。
 * @param tone tone 属性，表示该记录组件承载的数据。
 * @param visibility visibility 属性，表示该记录组件承载的数据。
 * @param attributes attributes 属性，表示该记录组件承载的数据。
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
