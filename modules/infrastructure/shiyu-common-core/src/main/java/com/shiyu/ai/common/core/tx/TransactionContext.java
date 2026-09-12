package com.shiyu.ai.common.core.tx;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * {@code TransactionContext} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public final class TransactionContext {

    private TransactionContext() {}

    /** 是否存在事务（已开启） */
    public static boolean isTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    /** 是否允许注册同步器 */
    public static boolean isSynchronizationActive() {
        return TransactionSynchronizationManager.isSynchronizationActive();
    }

    /** 当前是否只读事务 */
    public static boolean isReadOnly() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    /** 当前事务名称（调试/日志） */
    public static String getTransactionName() {
        return TransactionSynchronizationManager.getCurrentTransactionName();
    }
}
