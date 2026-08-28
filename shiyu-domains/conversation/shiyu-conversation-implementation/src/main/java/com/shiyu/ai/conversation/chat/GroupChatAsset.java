package com.shiyu.ai.conversation.chat;

import java.time.Instant;

public record GroupChatAsset(String id, long tenantId, long ownerUserId, GroupChat group, Instant createdAt, Instant updatedAt) { }
