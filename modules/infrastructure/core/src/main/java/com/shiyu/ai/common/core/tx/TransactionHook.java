package com.shiyu.ai.common.core.tx;

public interface TransactionHook {

    default void beforeCommit() {}

    default void afterCommit() {}

    default void afterRollback() {}

    default void afterCompletion() {}
}

