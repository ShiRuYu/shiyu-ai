package com.shiyu.ai.conversation.implementation.domain.chat;

import java.util.List;

/**
 * {@code GroupChat} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param participants participants 属性，表示该记录组件承载的数据。
 * @param speakerPolicy speakerPolicy 属性，表示该记录组件承载的数据。
 * @param maxTurns maxTurns 属性，表示该记录组件承载的数据。
 * @param tokenBudget tokenBudget 属性，表示该记录组件承载的数据。
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
     * {@code Participant} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param displayName displayName 属性，表示该记录组件承载的数据。
     * @param characterId characterId 属性，表示该记录组件承载的数据。
     */
    public record Participant(String id, String displayName, String characterId) {}
}
