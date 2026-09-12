package com.shiyu.ai.common.core.tx;

/**
 * TransactionHook 接口，定义基础设施模块的能力边界。
 */
public interface TransactionHook {

    /**
     * 执行 {@code beforeCommit} 定义的接口操作。
     */
    default void beforeCommit() {}

    /**
     * 执行 {@code afterCommit} 定义的接口操作。
     */
    default void afterCommit() {}

    /**
     * 执行 {@code afterRollback} 定义的接口操作。
     */
    default void afterRollback() {}

    /**
     * 执行 {@code afterCompletion} 定义的接口操作。
     */
    default void afterCompletion() {}
}
