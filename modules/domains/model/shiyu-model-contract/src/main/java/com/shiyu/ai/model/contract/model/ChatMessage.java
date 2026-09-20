package com.shiyu.ai.model.contract.model;

import java.util.List;

/**
 * 封装 对话 消息 相关的不可变数据及其字段约束。
 */
public record ChatMessage(String role, List<ContentPart> content) {
    public ChatMessage {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("message role is required");
        }
        content = content == null ? List.of() : List.copyOf(content);
    }

    /**
     * 封装 Content Part 相关的不可变数据及其字段约束。
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
         * 执行 Content Part 相关业务操作，并维护必要的状态和协作关系。
         *
         * @param type 用于完成本次业务处理的 type 参数。
         * @param text 用于完成本次业务处理的 text 参数。
         * @param uri 用于完成本次业务处理的 uri 参数。
         * @param mimeType 用于完成本次业务处理的 mimeType 参数。
         */
        public ContentPart(String type, String text, String uri, String mimeType) {
            this(type, text, uri, mimeType, null, null, null, null);
        }
    }

    /**
     * 执行 对话 消息 相关业务数据，并返回处理结果。
     *
     * @param role 用于完成本次业务处理的 role 参数。
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回 对话 消息 相关操作生成的结果数据。
     */
    public static ChatMessage text(String role, String text) {
        return new ChatMessage(
                role, List.of(new ContentPart("text", text == null ? "" : text, null, null)));
    }
}
