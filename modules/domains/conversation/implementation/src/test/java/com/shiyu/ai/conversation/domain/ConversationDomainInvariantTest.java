package com.shiyu.ai.conversation.domain;

import com.shiyu.ai.conversation.chat.PromptTemplateVersion;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversationDomainInvariantTest {
    @Test
    void validatesGenerationRunsAndTransitions() {
        Instant now = Instant.now();
        GenerationRun created = new GenerationRun("run", "conversation", "input", "assistant",
                "speaker", "OPENAI", "gpt", GenerationStatus.CREATED, 1, 2, 3,
                null, -1, false, 0, now, now, null);
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        assertEquals(GenerationStatus.RUNNING, running.status());
        assertEquals(1, running.version());
        assertEquals(GenerationStatus.COMPLETED, running.transition(GenerationStatus.COMPLETED).status());
        assertEquals("runtime", created.withRuntimeRunId("runtime").runtimeRunId());
        assertThrows(IllegalArgumentException.class, () -> created.transition(null));
        assertThrows(IllegalStateException.class, () -> created.transition(GenerationStatus.COMPLETED));
        assertThrows(IllegalArgumentException.class, () -> new GenerationRun("", "conversation", null, null,
                null, null, null, GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, now, now, null));
        assertThrows(IllegalArgumentException.class, () -> new GenerationRun("run", "", null, null,
                null, null, null, GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, now, now, null));
        assertThrows(IllegalArgumentException.class, () -> new GenerationRun("run", "conversation", null, null,
                null, null, null, null, 0, 0, 0, null, -1, false, 0, now, now, null));
        assertThrows(IllegalArgumentException.class, () -> new GenerationRun("run", "conversation", null, null,
                null, null, null, GenerationStatus.CREATED, 0, 0, 0, null, -2, false, 0, now, now, null));
    }

    @Test
    void validatesEventsAndPromptRevisions() {
        GenerationEvent event = new GenerationEvent("run", 0, GenerationEventType.DELTA, null, null);
        assertEquals("", event.payload());
        assertNotNull(event.createdAt());
        assertThrows(IllegalArgumentException.class, () -> new GenerationEvent("", 0, GenerationEventType.DELTA, "", null));
        assertThrows(IllegalArgumentException.class, () -> new GenerationEvent("run", -1, GenerationEventType.DELTA, "", null));
        assertThrows(IllegalArgumentException.class, () -> new GenerationEvent("run", 0, null, "", null));

        PromptTemplateVersion draft = new PromptTemplateVersion("v1", "prompt", 1, "DRAFT", "Hi",
                null, null, null, null);
        assertEquals(Map.of(), draft.variableSchema());
        assertEquals(List.of(), draft.testCases());
        assertThrows(IllegalArgumentException.class, () -> new PromptTemplateVersion("v0", "prompt", 0,
                "DRAFT", "", Map.of(), List.of(), null, null));
        assertThrows(IllegalArgumentException.class, () -> new PromptTemplateVersion("v2", "prompt", 1,
                "ARCHIVED", "", Map.of(), List.of(), null, null));
    }
}
