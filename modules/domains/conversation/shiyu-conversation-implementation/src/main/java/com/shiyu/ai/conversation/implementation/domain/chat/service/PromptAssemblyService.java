package com.shiyu.ai.conversation.implementation.domain.chat.service;

import com.shiyu.ai.conversation.implementation.domain.chat.model.LorebookEntry;

import com.shiyu.ai.conversation.implementation.domain.chat.model.CharacterCardV2;

import com.shiyu.ai.conversation.implementation.domain.chat.model.Persona;

import com.shiyu.ai.conversation.implementation.domain.model.ContentPart;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;
import com.shiyu.ai.conversation.implementation.domain.model.MessageRole;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 提供 提示词 Assembly 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Service
public class PromptAssemblyService {
    /**
     * 构建或转换 提示词 Assembly 相关业务数据，并返回处理结果。
     *
     * @param platformSafety 用于完成本次业务处理的 platformSafety 参数。
     * @param conversationSystem 用于完成本次业务处理的 conversationSystem 参数。
     * @param character 用于完成本次业务处理的 character 参数。
     * @param persona 用于完成本次业务处理的 persona 参数。
     * @param lorebook 用于完成本次业务处理的 lorebook 参数。
     * @param magma 用于完成本次业务处理的 magma 参数。
     * @param history 用于完成本次业务处理的 history 参数。
     * @param current 用于完成本次业务处理的 current 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 构建或转换 提示词 Assembly 相关业务数据，并返回处理结果。
     *
     * @param platformSafety 用于完成本次业务处理的 platformSafety 参数。
     * @param conversationSystem 用于完成本次业务处理的 conversationSystem 参数。
     * @param character 用于完成本次业务处理的 character 参数。
     * @param persona 用于完成本次业务处理的 persona 参数。
     * @param lorebook 用于完成本次业务处理的 lorebook 参数。
     * @param magma 用于完成本次业务处理的 magma 参数。
     * @param history 用于完成本次业务处理的 history 参数。
     * @param current 用于完成本次业务处理的 current 参数。
     * @param query 用于筛选目标数据的查询条件。
     * @param lorebookTokenBudget 用于完成本次业务处理的 lorebookTokenBudget 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
