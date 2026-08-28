package com.shiyu.ai.conversation.port;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.Optional;

public interface IdempotencyRepository {
    Optional<String> find(TenantId tenantId, long ownerUserId, String operation, String key);
    boolean claim(TenantId tenantId, long ownerUserId, String operation, String key, String resourceId);
}
