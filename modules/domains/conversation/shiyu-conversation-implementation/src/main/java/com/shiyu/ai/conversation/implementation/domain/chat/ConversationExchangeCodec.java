package com.shiyu.ai.conversation.implementation.domain.chat;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@code ConversationExchangeCodec} 承载会话模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public final class ConversationExchangeCodec {
    private ConversationExchangeCodec() {}

    /**
     * {@code toJsonl} 将当前对象转换为目标表示形式。
     *
     * @param messages 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String toJsonl(List<ConversationMessage> messages) {
        return messages.stream().map(JSONUtils::toJsonString).collect(Collectors.joining("\n"));
    }

    /**
     * {@code toMarkdown} 将当前对象转换为目标表示形式。
     *
     * @param messages 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String toMarkdown(List<ConversationMessage> messages) {
        return messages.stream()
                .map(
                        message ->
                                "## "
                                        + message.role().name()
                                        + "\n\n"
                                        + message.textContent()
                                        + "\n")
                .collect(Collectors.joining("\n"));
    }

    /**
     * 处理jsonl。
     *
     * @return 结果列表。
     */
    @SuppressWarnings("unchecked")
    public static List<ImportedMessage> fromJsonl(String jsonl) {
        if (jsonl == null || jsonl.isBlank()) return List.of();
        List<ImportedMessage> result = new ArrayList<>();
        for (String line : jsonl.split("\\R")) {
            if (line.isBlank()) continue;
            Map<String, Object> value = JSONUtils.parseObject(line, Map.class);
            String role = String.valueOf(value.getOrDefault("role", "USER"));
            String text = String.valueOf(value.getOrDefault("textContent", ""));
            if (text.isBlank() && value.get("contentParts") instanceof List<?> parts) {
                text =
                        parts.stream()
                                .filter(Map.class::isInstance)
                                .map(Map.class::cast)
                                .filter(part -> "text".equals(String.valueOf(part.get("type"))))
                                .map(part -> String.valueOf(part.getOrDefault("text", "")))
                                .collect(Collectors.joining());
            }
            result.add(new ImportedMessage(role.toUpperCase(Locale.ROOT), text));
        }
        return List.copyOf(result);
    }

    /**
     * {@code fromMarkdown} 执行当前类型定义的业务操作。
     *
     * @param markdown 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static List<ImportedMessage> fromMarkdown(String markdown) {
        if (markdown == null || markdown.isBlank()) return List.of();
        List<ImportedMessage> result = new ArrayList<>();
        String role = "USER";
        StringBuilder text = new StringBuilder();
        for (String line : markdown.split("\\R")) {
            if (line.startsWith("## ")) {
                if (!text.isEmpty()) result.add(new ImportedMessage(role, text.toString().trim()));
                role = line.substring(3).trim().toUpperCase(Locale.ROOT);
                text.setLength(0);
            } else text.append(line).append('\n');
        }
        if (!text.isEmpty()) result.add(new ImportedMessage(role, text.toString().trim()));
        return List.copyOf(result);
    }

    /**
     * {@code ImportedMessage} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param role 角色，表示该记录组件承载的数据。
     * @param content 内容，表示该记录组件承载的数据。
     */
    public record ImportedMessage(String role, String content) {}
}
