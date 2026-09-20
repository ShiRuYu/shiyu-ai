package com.shiyu.ai.conversation.implementation.domain.chat.service;

import com.shiyu.ai.conversation.implementation.domain.chat.model.GroupChat;

import com.shiyu.ai.conversation.implementation.domain.chat.model.SpeakerPolicy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现 Group Turn Planner 所属领域的业务规则和状态变化。
 */
public final class GroupTurnPlanner {
    private GroupTurnPlanner() {}

    /**
     * 执行 Group Turn Planner 相关业务数据，并返回处理结果。
     *
     * @param group 用于完成本次业务处理的 group 参数。
     * @param completedSpeakerIds 待处理的业务对象标识集合。
     * @param requestedSpeakerId 用于定位requested Speaker的标识。
     * @param consumedTokens 用于完成本次业务处理的 consumedTokens 参数。
     * @return 返回 Group Turn Planner 相关操作生成的结果数据。
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
     * 封装 Turn Decision 相关的不可变数据及其字段约束。
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
