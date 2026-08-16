package com.shiyu.ai.chat.product;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupTurnPlannerTest {
    private static final List<GroupChat.Participant> PARTICIPANTS = List.of(
            new GroupChat.Participant("a", "A", "character-a"),
            new GroupChat.Participant("b", "B", "character-b"));

    @Test
    void roundRobinAndMaxTurnsAreBounded() {
        GroupChat group = new GroupChat("g", "test", PARTICIPANTS, SpeakerPolicy.ROUND_ROBIN, 2, 100);
        assertEquals("a", GroupTurnPlanner.next(group, List.of(), null, 0).participant().id());
        assertEquals("b", GroupTurnPlanner.next(group, List.of("a"), null, 20).participant().id());
        assertTrue(GroupTurnPlanner.next(group, List.of("a", "b"), null, 20).exhausted());
    }

    @Test
    void manualRequiresParticipantAndModelRoutedBalancesTurns() {
        GroupChat manual = new GroupChat("g", "test", PARTICIPANTS, SpeakerPolicy.MANUAL, 4, 100);
        assertThrows(IllegalArgumentException.class, () -> GroupTurnPlanner.next(manual, List.of(), "unknown", 0));
        GroupChat routed = new GroupChat("g", "test", PARTICIPANTS, SpeakerPolicy.MODEL_ROUTED, 4, 100);
        assertEquals("b", GroupTurnPlanner.next(routed, List.of("a"), null, 0).participant().id());
    }
}
