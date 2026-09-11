package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Optional;

/** Tenant-scoped claim store preventing duplicate command execution. */
public interface IdempotencyRepository {
    Optional<String> find(TenantId tenantId, long ownerUserId, String operation, String key);

    boolean claim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId);
}
