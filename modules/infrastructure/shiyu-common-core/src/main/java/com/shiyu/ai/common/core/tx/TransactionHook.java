package com.shiyu.ai.common.core.tx;

/**
 * 定义 Transaction Hook 相关的协作契约和调用边界。
 */
public interface TransactionHook {

    /**
     * 执行 Transaction Hook 相关业务操作，并维护必要的状态和协作关系。
     */
    default void beforeCommit() {}

    /**
     * 执行 Transaction Hook 相关业务操作，并维护必要的状态和协作关系。
     */
    default void afterCommit() {}

    /**
     * 执行 Transaction Hook 相关业务操作，并维护必要的状态和协作关系。
     */
    default void afterRollback() {}

    /**
     * 执行 Transaction Hook 相关业务操作，并维护必要的状态和协作关系。
     */
    default void afterCompletion() {}
}
