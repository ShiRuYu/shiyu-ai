package com.shiyu.ai.conversation.chat;

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

    @Test
    void rejectsInvalidGroupsAndExhaustsBudget() {
        assertThrows(IllegalArgumentException.class,
                () -> GroupTurnPlanner.next(null, List.of(), null, 0));
        GroupChat defaults = new GroupChat("g", "defaults", null, null, 2, 100);
        assertTrue(defaults.participants().isEmpty());
        GroupChat empty = new GroupChat("g", "empty", List.of(), null, 2, 100);
        assertTrue(GroupTurnPlanner.next(empty, null, null, 0).exhausted());
        GroupChat group = new GroupChat("g", "test", PARTICIPANTS, SpeakerPolicy.ROUND_ROBIN, 2, 100);
        assertTrue(GroupTurnPlanner.next(group, List.of(), null, -1).exhausted());
        assertTrue(GroupTurnPlanner.next(group, List.of(), null, 100).exhausted());
        GroupChat manual = new GroupChat("g", "test", PARTICIPANTS, SpeakerPolicy.MANUAL, 2, 100);
        assertThrows(IllegalArgumentException.class,
                () -> GroupTurnPlanner.next(manual, List.of(), null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new GroupChat("g", "bad", List.of(new GroupChat.Participant("", "", "")), null, 2, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new GroupChat("g", "bad", List.of(PARTICIPANTS.get(0), PARTICIPANTS.get(0)), null, 2, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new GroupChat("g", "bad", java.util.Collections.nCopies(33, PARTICIPANTS.get(0)), null, 2, 100));
        assertThrows(NullPointerException.class,
                () -> new GroupChat("g", "bad", java.util.Arrays.asList((GroupChat.Participant) null), null, 2, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new GroupChat("g", "bad", PARTICIPANTS, null, 0, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new GroupChat("g", "bad", PARTICIPANTS, null, 2, 100_001));
    }
}
