package com.shiyu.ai.conversation.implementation.domain.chat;

import com.shiyu.ai.conversation.implementation.domain.model.ContentPart;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;
import com.shiyu.ai.conversation.implementation.domain.model.MessageRole;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * PromptAssemblyService 服务接口，负责执行会话领域相关业务操作。
 */
@Service
public class PromptAssemblyService {
    /**
     * {@code assemble} 执行当前类型定义的业务操作。
     *
     * @param platformSafety 参数值，用于执行当前操作。
     * @param conversationSystem 参数值，用于执行当前操作。
     * @param character 参数值，用于执行当前操作。
     * @param persona 参数值，用于执行当前操作。
     * @param lorebook 参数值，用于执行当前操作。
     * @param magma 参数值，用于执行当前操作。
     * @param history 参数值，用于执行当前操作。
     * @param current 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<ConversationMessage> assemble(
            List<ConversationMessage> platformSafety,
            List<ConversationMessage> conversationSystem,
            CharacterCardV2 character,
            Persona persona,
            List<LorebookEntry> lorebook,
            List<ConversationMessage> magma,
            List<ConversationMessage> history,
            ConversationMessage current) {
        List<ConversationMessage> result = new ArrayList<>();
        result.addAll(platformSafety == null ? List.of() : platformSafety);
        result.addAll(conversationSystem == null ? List.of() : conversationSystem);
        if (character != null)
            result.add(
                    system(
                            "character",
                            character.name()
                                    + "\n"
                                    + character.description()
                                    + "\n"
                                    + character.systemPrompt()));
        if (persona != null)
            result.add(
                    system(
                            "persona",
                            persona.name() + "\n" + persona.identity() + "\n" + persona.tone()));
        if (lorebook != null)
            lorebook.stream()
                    .filter(LorebookEntry::enabled)
                    .sorted(java.util.Comparator.comparingInt(LorebookEntry::priority).reversed())
                    .forEach(e -> result.add(system("lorebook", e.content())));
        result.addAll(magma == null ? List.of() : magma);
        result.addAll(history == null ? List.of() : history);
        if (current != null) result.add(current);
        return List.copyOf(result);
    }

    /**
     * 构建promptassembly。
     *
     * @param platformSafety platformSafety 参数。
     * @param conversationSystem conversationSystem 参数。
     * @param character character 参数。
     * @param persona persona 参数。
     * @param lorebook lorebook 参数。
     * @param magma magma 参数。
     * @param history history 参数。
     * @param current current 参数。
     * @param query query 参数。
     * @param lorebookTokenBudget lorebookTokenBudget 参数。
     *
     * @return 结果列表。
     */
    public List<ConversationMessage> assemble(
            List<ConversationMessage> platformSafety,
            List<ConversationMessage> conversationSystem,
            CharacterCardV2 character,
            Persona persona,
            List<LorebookEntry> lorebook,
            List<ConversationMessage> magma,
            List<ConversationMessage> history,
            ConversationMessage current,
            String query,
            int lorebookTokenBudget) {
        List<LorebookEntry> selected =
                (lorebook == null ? List.<LorebookEntry>of() : lorebook)
                        .stream()
                                .filter(LorebookEntry::enabled)
                                .filter(
                                        entry ->
                                                query == null
                                                        || query.isBlank()
                                                        || entry.keys().isEmpty()
                                                        || entry.keys().stream()
                                                                .anyMatch(
                                                                        key ->
                                                                                query.toLowerCase(
                                                                                                java
                                                                                                        .util
                                                                                                        .Locale
                                                                                                        .ROOT)
                                                                                        .contains(
                                                                                                key
                                                                                                        .toLowerCase(
                                                                                                                java
                                                                                                                        .util
                                                                                                                        .Locale
                                                                                                                        .ROOT))))
                                .sorted(
                                        java.util.Comparator.comparingInt(LorebookEntry::priority)
                                                .reversed())
                                .toList();
        List<LorebookEntry> withinBudget = new ArrayList<>();
        int used = 0;
        int budget = Math.max(0, lorebookTokenBudget);
        for (LorebookEntry entry : selected) {
            int estimate = Math.max(1, entry.content() == null ? 0 : entry.content().length() / 4);
            int cap = entry.tokenBudget() > 0 ? Math.min(entry.tokenBudget(), estimate) : estimate;
            if (used + cap > budget) continue;
            String content = entry.content() == null ? "" : entry.content();
            if (entry.tokenBudget() > 0 && content.length() > entry.tokenBudget() * 4) {
                content = content.substring(0, Math.max(1, entry.tokenBudget() * 4));
            }
            withinBudget.add(
                    new LorebookEntry(
                            entry.id(),
                            entry.keys(),
                            content,
                            entry.priority(),
                            entry.insertionPosition(),
                            entry.tokenBudget(),
                            entry.enabled()));
            used += cap;
        }
        return assemble(
                platformSafety,
                conversationSystem,
                character,
                persona,
                withinBudget,
                magma,
                history,
                current);
    }

    private ConversationMessage system(String source, String text) {
        return new ConversationMessage(
                source,
                "prompt-preview",
                null,
                null,
                MessageRole.SYSTEM,
                List.of(ContentPart.text(text)),
                java.util.Map.of(),
                com.shiyu.ai.conversation.implementation.domain.model.MessageStatus.COMPLETED,
                0,
                null,
                java.time.Instant.now(),
                java.time.Instant.now());
    }
}
