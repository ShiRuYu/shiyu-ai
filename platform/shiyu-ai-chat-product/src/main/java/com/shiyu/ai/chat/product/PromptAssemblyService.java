package com.shiyu.ai.chat.product;

import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.ContentPart;
import com.shiyu.ai.conversation.domain.MessageRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Keeps product prompt ordering explicit and inspectable for Prompt Preview. */
@Service
public class PromptAssemblyService {
    public List<ConversationMessage> assemble(List<ConversationMessage> platformSafety,
                                              List<ConversationMessage> conversationSystem,
                                              CharacterCardV2 character, Persona persona,
                                              List<LorebookEntry> lorebook, List<ConversationMessage> magma,
                                              List<ConversationMessage> history, ConversationMessage current) {
        List<ConversationMessage> result = new ArrayList<>();
        result.addAll(platformSafety == null ? List.of() : platformSafety);
        result.addAll(conversationSystem == null ? List.of() : conversationSystem);
        if (character != null) result.add(system("character", character.name() + "\n" + character.description() + "\n" + character.systemPrompt()));
        if (persona != null) result.add(system("persona", persona.name() + "\n" + persona.identity() + "\n" + persona.tone()));
        if (lorebook != null) lorebook.stream().filter(LorebookEntry::enabled).sorted(java.util.Comparator.comparingInt(LorebookEntry::priority).reversed()).forEach(e -> result.add(system("lorebook", e.content())));
        result.addAll(magma == null ? List.of() : magma);
        result.addAll(history == null ? List.of() : history);
        if (current != null) result.add(current);
        return List.copyOf(result);
    }

    /** Query-aware assembly used by Prompt Studio and runtime; caps Lorebook expansion deterministically. */
    public List<ConversationMessage> assemble(List<ConversationMessage> platformSafety,
                                              List<ConversationMessage> conversationSystem,
                                              CharacterCardV2 character, Persona persona,
                                              List<LorebookEntry> lorebook, List<ConversationMessage> magma,
                                              List<ConversationMessage> history, ConversationMessage current,
                                              String query, int lorebookTokenBudget) {
        List<LorebookEntry> selected = (lorebook == null ? List.<LorebookEntry>of() : lorebook).stream()
                .filter(LorebookEntry::enabled)
                .filter(entry -> query == null || query.isBlank() || entry.keys().isEmpty() ||
                        entry.keys().stream().anyMatch(key -> query.toLowerCase(java.util.Locale.ROOT).contains(key.toLowerCase(java.util.Locale.ROOT))))
                .sorted(java.util.Comparator.comparingInt(LorebookEntry::priority).reversed())
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
            withinBudget.add(new LorebookEntry(entry.id(), entry.keys(), content, entry.priority(), entry.insertionPosition(), entry.tokenBudget(), entry.enabled()));
            used += cap;
        }
        return assemble(platformSafety, conversationSystem, character, persona, withinBudget, magma, history, current);
    }
    private ConversationMessage system(String source, String text) {
        return new ConversationMessage(source, "prompt-preview", null, null, MessageRole.SYSTEM, List.of(ContentPart.text(text)), java.util.Map.of(), com.shiyu.ai.conversation.domain.MessageStatus.COMPLETED, 0, null, java.time.Instant.now(), java.time.Instant.now());
    }
}
