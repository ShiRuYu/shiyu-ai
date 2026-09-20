package com.shiyu.ai.conversation.implementation.domain.chat.model;

import java.time.Instant;

/**
 * 封装 Group 对话 Asset 相关的不可变数据及其字段约束。
 */
public record GroupChatAsset(
        String id,
        long tenantId,
        long ownerUserId,
        GroupChat group,
        Instant createdAt,
        Instant updatedAt) {}
