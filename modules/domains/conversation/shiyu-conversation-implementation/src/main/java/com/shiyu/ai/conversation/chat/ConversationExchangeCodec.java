package com.shiyu.ai.conversation.chat;

import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.common.core.utils.JSONUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConversationExchangeCodec {
    private ConversationExchangeCodec() { }

    public static String toJsonl(List<ConversationMessage> messages) {
        return messages.stream().map(JSONUtils::toJsonString).collect(Collectors.joining("\n"));
    }

    public static String toMarkdown(List<ConversationMessage> messages) {
        return messages.stream().map(message -> "## " + message.role().name() + "\n\n" + message.textContent() + "\n")
                .collect(Collectors.joining("\n"));
    }

    /** Parses the portable JSONL export without trusting ids or conversation ownership. */
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
                text = parts.stream().filter(Map.class::isInstance).map(Map.class::cast)
                        .filter(part -> "text".equals(String.valueOf(part.get("type"))))
                        .map(part -> String.valueOf(part.getOrDefault("text", ""))).collect(Collectors.joining());
            }
            result.add(new ImportedMessage(role.toUpperCase(Locale.ROOT), text));
        }
        return List.copyOf(result);
    }

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

    public record ImportedMessage(String role, String content) { }
}
