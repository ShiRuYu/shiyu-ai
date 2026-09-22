package com.shiyu.ai.conversation.implementation.domain.chat.codec;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 解析或编解码 会话 Exchange 相关的外部内容和领域数据。
 */
public final class ConversationExchangeCodec {
    private ConversationExchangeCodec() {}

    /**
     * 构建或转换 会话 Exchange 相关业务数据，并返回处理结果。
     *
     * @param messages 用于完成本次业务处理的 messages 参数。
     * @return 返回 会话 Exchange 相关操作生成的结果数据。
     */
    public static String toJsonl(List<ConversationMessage> messages) {
        return messages.stream().map(JSONUtils::toJsonString).collect(Collectors.joining("\n"));
    }

    /**
     * 构建或转换 会话 Exchange 相关业务数据，并返回处理结果。
     *
     * @param messages 用于完成本次业务处理的 messages 参数。
     * @return 返回 会话 Exchange 相关操作生成的结果数据。
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
     * 执行 会话 Exchange 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param unchecked 用于完成本次业务处理的 unchecked 参数。
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
     * 执行 会话 Exchange 相关业务数据，并返回处理结果。
     *
     * @param markdown 用于完成本次业务处理的 markdown 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 封装 Imported 消息 相关的不可变数据及其字段约束。
     */
    public record ImportedMessage(String role, String content) {}
}
