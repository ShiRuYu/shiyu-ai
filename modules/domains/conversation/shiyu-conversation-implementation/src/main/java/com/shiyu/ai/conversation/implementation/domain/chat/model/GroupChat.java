package com.shiyu.ai.conversation.implementation.domain.chat.model;

import java.util.List;

/**
 * 封装 Group 对话 相关的不可变数据及其字段约束。
 */
public record GroupChat(
        String id,
        String name,
        List<Participant> participants,
        SpeakerPolicy speakerPolicy,
        int maxTurns,
        int tokenBudget) {
    public GroupChat {
        participants = participants == null ? List.of() : List.copyOf(participants);
        if (participants.size() > 32)
            throw new IllegalArgumentException("group cannot have more than 32 participants");
        if (participants.stream().anyMatch(p -> p == null || p.id() == null || p.id().isBlank()))
            throw new IllegalArgumentException("participant id is required");
        if (participants.stream().map(Participant::id).distinct().count() != participants.size())
            throw new IllegalArgumentException("participant ids must be unique");
        if (speakerPolicy == null) speakerPolicy = SpeakerPolicy.MANUAL;
        if (maxTurns < 1 || maxTurns > 100)
            throw new IllegalArgumentException("maxTurns must be between 1 and 100");
        if (tokenBudget < 1 || tokenBudget > 100_000)
            throw new IllegalArgumentException("tokenBudget must be between 1 and 100000");
    }

    /**
     * 封装 Participant 相关的不可变数据及其字段约束。
     */
    public record Participant(String id, String displayName, String characterId) {}
}
