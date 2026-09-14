package com.shiyu.ai.conversation.implementation.domain.chat;

import java.util.List;

/**
 * {@code LorebookEntry} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param keys keys 属性，表示该记录组件承载的数据。
 * @param content 内容，表示该记录组件承载的数据。
 * @param priority priority 属性，表示该记录组件承载的数据。
 * @param insertionPosition insertionPosition 属性，表示该记录组件承载的数据。
 * @param tokenBudget tokenBudget 属性，表示该记录组件承载的数据。
 * @param enabled enabled 属性，表示该记录组件承载的数据。
 */
public record LorebookEntry(
        String id,
        List<String> keys,
        String content,
        int priority,
        String insertionPosition,
        int tokenBudget,
        boolean enabled) {
    public LorebookEntry {
        keys = keys == null ? List.of() : List.copyOf(keys);
    }
}
