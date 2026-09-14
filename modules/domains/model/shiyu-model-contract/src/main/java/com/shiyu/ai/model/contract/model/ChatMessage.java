package com.shiyu.ai.model.contract.model;

import java.util.List;

/**
 * 表示一次对话消息及其结构化内容。
 * @param role 角色，表示该记录组件承载的数据。
 * @param content 内容，表示该记录组件承载的数据。
 */
public record ChatMessage(String role, List<ContentPart> content) {
    public ChatMessage {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("message role is required");
        }
        content = content == null ? List.of() : List.copyOf(content);
    }

    /**
     * {@code ContentPart} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param type 类型，表示该记录组件承载的数据。
     * @param text text 属性，表示该记录组件承载的数据。
     * @param uri 资源地址，表示该记录组件承载的数据。
     * @param mimeType mimeType 属性，表示该记录组件承载的数据。
     * @param toolCallId toolCallId 属性，表示该记录组件承载的数据。
     * @param toolName toolName 属性，表示该记录组件承载的数据。
     * @param toolArguments toolArguments 属性，表示该记录组件承载的数据。
     * @param index index 属性，表示该记录组件承载的数据。
     */
    public record ContentPart(
            String type,
            String text,
            String uri,
            String mimeType,
            String toolCallId,
            String toolName,
            String toolArguments,
            Integer index) {
        /**
         * {@code ContentPart} 创建并初始化当前类型实例。
         *
         * @param type 参数值，用于执行当前操作。
         * @param text 参数值，用于执行当前操作。
         * @param uri 参数值，用于执行当前操作。
         * @param mimeType 参数值，用于执行当前操作。
         */
        public ContentPart(String type, String text, String uri, String mimeType) {
            this(type, text, uri, mimeType, null, null, null, null);
        }
    }

    /**
     * {@code text} 执行当前类型定义的业务操作。
     *
     * @param role 参数值，用于执行当前操作。
     * @param text 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static ChatMessage text(String role, String text) {
        return new ChatMessage(
                role, List.of(new ContentPart("text", text == null ? "" : text, null, null)));
    }
}
