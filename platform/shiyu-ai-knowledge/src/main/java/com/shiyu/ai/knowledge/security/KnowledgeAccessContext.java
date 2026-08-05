package com.shiyu.ai.knowledge.security;

import java.io.Serializable;

/**
 * Immutable security context captured at the Agent execution boundary.
 * Knowledge services must use this context instead of reading request state
 * from a worker thread.
 */
public record KnowledgeAccessContext(
        Long tenantId,
        Long userId,
        Long roleId,
        boolean superAdmin
) implements Serializable {
}
