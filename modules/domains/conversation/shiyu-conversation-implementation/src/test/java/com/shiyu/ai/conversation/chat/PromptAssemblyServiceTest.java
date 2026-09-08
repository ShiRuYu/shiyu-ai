package com.shiyu.ai.conversation.chat;

import com.shiyu.ai.conversation.domain.ContentPart;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.MessageRole;
import com.shiyu.ai.conversation.domain.MessageStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromptAssemblyServiceTest {
    private final PromptAssemblyService service = new PromptAssemblyService();

    @Test
    void assemblesOptionalSourcesInStableOrderAndSkipsDisabledLorebookEntries() {
        ConversationMessage safety = message("safety", MessageRole.SYSTEM);
        ConversationMessage history = message("history", MessageRole.USER);
        CharacterCardV2 character = new CharacterCardV2(null, "Ava", "desc", "scene", "hello", null, "system", null, 0);
        Persona persona = new Persona("p", 8, "User", "student", "warm", "PRIVATE", null);
        LorebookEntry low = new LorebookEntry("low", List.of(), "low", 1, "before", 0, true);
        LorebookEntry disabled = new LorebookEntry("disabled", List.of(), "hidden", 99, "before", 0, false);
        LorebookEntry high = new LorebookEntry("high", List.of(), "high", 10, "before", 0, true);
        List<ConversationMessage> result = service.assemble(List.of(safety), null, character, persona,
                List.of(low, disabled, high), null, List.of(history), null);

        assertEquals(List.of("safety", "Ava\ndesc\nsystem", "User\nstudent\nwarm", "high", "low", "history"),
                result.stream().map(ConversationMessage::textContent).toList());
        assertTrue(result.stream().allMatch(item -> item.role() != MessageRole.ASSISTANT));
    }

    @Test
    void queryAwareAssemblyFiltersKeysCapsEntriesAndHandlesEmptyInputs() {
        LorebookEntry matched = new LorebookEntry("matched", List.of("Magic"), "1234567890", 10, "before", 1, true);
        LorebookEntry noKey = new LorebookEntry("no-key", List.of(), "free", 5, "before", 0, true);
        LorebookEntry unmatched = new LorebookEntry("unmatched", List.of("science"), "skip", 20, "before", 0, true);
        LorebookEntry tooLarge = new LorebookEntry("large", List.of("magic"), "skip-budget", 1, "before", 10, true);
        List<ConversationMessage> result = service.assemble(null, null, null, null,
                List.of(unmatched, matched, noKey, tooLarge), null, null, null, "magic", 3);
        assertEquals(List.of("1234", "free"), result.stream().map(ConversationMessage::textContent).toList());

        List<ConversationMessage> blankQuery = service.assemble(List.of(), List.of(), null, null,
                List.of(new LorebookEntry("null", null, null, 1, "before", 0, true)),
                List.of(), List.of(), null, "", 1);
        assertEquals(1, blankQuery.size());
        assertEquals("", blankQuery.getFirst().textContent());
    }

    private static ConversationMessage message(String text, MessageRole role) {
        Instant now = Instant.now();
        return new ConversationMessage(text, "c", null, null, role,
                List.of(ContentPart.text(text)), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);
    }
}
