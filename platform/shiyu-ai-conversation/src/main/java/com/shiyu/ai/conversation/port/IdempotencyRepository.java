package com.shiyu.ai.conversation.port;

import java.util.Optional;

public interface IdempotencyRepository {
    Optional<String> find(long tenantId, long ownerUserId, String operation, String key);
    boolean claim(long tenantId, long ownerUserId, String operation, String key, String resourceId);
}
