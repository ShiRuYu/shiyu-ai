package com.shiyu.ai.iam.implementation.infrastructure.auth;

/** Atomically revokes a persisted token and returns its owner when successful. */
public interface AtomicTokenConsumer {
    Long consumeToken(String tokenValue);
}
