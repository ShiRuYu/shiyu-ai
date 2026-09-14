package com.shiyu.ai.conversation.implementation.domain.chat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 规划群聊参与者的轮次和发言顺序。
 */
public final class GroupTurnPlanner {
    private GroupTurnPlanner() {}

    /**
     * {@code next} 执行当前类型定义的业务操作。
     *
     * @param group 参数值，用于执行当前操作。
     * @param completedSpeakerIds 参数值，用于执行当前操作。
     * @param requestedSpeakerId 参数值，用于执行当前操作。
     * @param consumedTokens 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static TurnDecision next(
            GroupChat group,
            List<String> completedSpeakerIds,
            String requestedSpeakerId,
            int consumedTokens) {
        if (group == null) throw new IllegalArgumentException("group is required");
        List<String> completed =
                completedSpeakerIds == null ? List.of() : List.copyOf(completedSpeakerIds);
        if (completed.size() >= group.maxTurns()) return TurnDecision.exhausted("maxTurns reached");
        if (consumedTokens < 0 || consumedTokens >= group.tokenBudget())
            return TurnDecision.exhausted("token budget reached");
        if (group.participants().isEmpty())
            return TurnDecision.exhausted("group has no participants");

        String selected =
                switch (group.speakerPolicy()) {
                    case MANUAL -> requestedSpeakerId;
                    case ROUND_ROBIN ->
                            group.participants()
                                    .get(completed.size() % group.participants().size())
                                    .id();
                    case MODEL_ROUTED -> leastUsed(group, completed);
                };
        if (selected == null
                || group.participants().stream().noneMatch(p -> selected.equals(p.id()))) {
            throw new IllegalArgumentException("speaker must be a group participant");
        }
        GroupChat.Participant participant =
                group.participants().stream()
                        .filter(p -> selected.equals(p.id()))
                        .findFirst()
                        .orElseThrow();
        return TurnDecision.selected(
                participant,
                group.maxTurns() - completed.size(),
                group.tokenBudget() - consumedTokens);
    }

    private static String leastUsed(GroupChat group, List<String> completed) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        group.participants().forEach(p -> counts.put(p.id(), 0));
        completed.forEach(id -> counts.computeIfPresent(id, (ignored, count) -> count + 1));
        return counts.entrySet().stream().min(Map.Entry.comparingByValue()).orElseThrow().getKey();
    }

    /**
     * {@code TurnDecision} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param exhausted exhausted 属性，表示该记录组件承载的数据。
     * @param reason reason 属性，表示该记录组件承载的数据。
     * @param participant participant 属性，表示该记录组件承载的数据。
     * @param remainingTurns remainingTurns 属性，表示该记录组件承载的数据。
     * @param remainingTokens remainingTokens 属性，表示该记录组件承载的数据。
     */
    public record TurnDecision(
            boolean exhausted,
            String reason,
            GroupChat.Participant participant,
            int remainingTurns,
            int remainingTokens) {
        static TurnDecision exhausted(String reason) {
            return new TurnDecision(true, reason, null, 0, 0);
        }

        static TurnDecision selected(
                GroupChat.Participant participant, int remainingTurns, int remainingTokens) {
            return new TurnDecision(false, null, participant, remainingTurns, remainingTokens);
        }
    }
}
