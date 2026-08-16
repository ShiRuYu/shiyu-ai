package com.shiyu.ai.chat.product;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bounded speaker selection for a group conversation. It deliberately only
 * plans one turn; the caller must persist the resulting GenerationRun before
 * asking for another turn.
 */
public final class GroupTurnPlanner {
    private GroupTurnPlanner() { }

    public static TurnDecision next(GroupChat group, List<String> completedSpeakerIds,
                                    String requestedSpeakerId, int consumedTokens) {
        if (group == null) throw new IllegalArgumentException("group is required");
        List<String> completed = completedSpeakerIds == null ? List.of() : List.copyOf(completedSpeakerIds);
        if (completed.size() >= group.maxTurns()) return TurnDecision.exhausted("maxTurns reached");
        if (consumedTokens < 0 || consumedTokens >= group.tokenBudget()) return TurnDecision.exhausted("token budget reached");
        if (group.participants().isEmpty()) return TurnDecision.exhausted("group has no participants");

        String selected = switch (group.speakerPolicy()) {
            case MANUAL -> requestedSpeakerId;
            case ROUND_ROBIN -> group.participants().get(completed.size() % group.participants().size()).id();
            case MODEL_ROUTED -> leastUsed(group, completed);
        };
        if (selected == null || group.participants().stream().noneMatch(p -> selected.equals(p.id()))) {
            throw new IllegalArgumentException("speaker must be a group participant");
        }
        GroupChat.Participant participant = group.participants().stream().filter(p -> selected.equals(p.id())).findFirst().orElseThrow();
        return TurnDecision.selected(participant, group.maxTurns() - completed.size(), group.tokenBudget() - consumedTokens);
    }

    private static String leastUsed(GroupChat group, List<String> completed) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        group.participants().forEach(p -> counts.put(p.id(), 0));
        completed.forEach(id -> counts.computeIfPresent(id, (ignored, count) -> count + 1));
        return counts.entrySet().stream().min(Map.Entry.comparingByValue()).orElseThrow().getKey();
    }

    public record TurnDecision(boolean exhausted, String reason, GroupChat.Participant participant,
                               int remainingTurns, int remainingTokens) {
        static TurnDecision exhausted(String reason) { return new TurnDecision(true, reason, null, 0, 0); }
        static TurnDecision selected(GroupChat.Participant participant, int remainingTurns, int remainingTokens) {
            return new TurnDecision(false, null, participant, remainingTurns, remainingTokens);
        }
    }
}
