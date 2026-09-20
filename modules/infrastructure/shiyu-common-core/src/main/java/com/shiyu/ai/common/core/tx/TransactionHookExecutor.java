package com.shiyu.ai.common.core.tx;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

/**
 * 实现 Transaction Hook Executor 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class TransactionHookExecutor {

    private TransactionHookExecutor() {}

    /**
     * 注册事务钩子（框架级入口）
     *
     * @param hook 事务钩子
     * @param fallbackWithoutTx 无事务是否立即执行
     */
    public static void register(TransactionHook hook, boolean fallbackWithoutTx) {
        Objects.requireNonNull(hook, "TransactionHook must not be null");

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            if (fallbackWithoutTx) {
                // 高可用降级：无事务直接执行
                hook.afterCommit();
                hook.afterCompletion();
                return;
            }
            throw new IllegalStateException("No active transaction for TransactionHook");
        }

        TransactionSynchronizationManager.registerSynchronization(
                new InternalSynchronization(hook));
    }

    /** 默认：无事务不执行（严格模式） */
    public static void register(TransactionHook hook) {
        register(hook, false);
    }

    /**
     * 实现 Internal Synchronization 相关的业务处理、协作逻辑或基础设施能力。
     */
    private static final class InternalSynchronization implements TransactionSynchronization {

        /**
         * hook 属性，保存当前对象中的业务数据或协作依赖。
         */
        private final TransactionHook hook;

        private InternalSynchronization(TransactionHook hook) {
            this.hook = hook;
        }

        /**
         * 执行 Internal Synchronization 相关业务操作，并维护必要的状态和协作关系。
         *
         * @param readOnly 用于完成本次业务处理的 readOnly 参数。
         */
        @Override
        public void beforeCommit(boolean readOnly) {
            hook.beforeCommit();
        }

        /**
         * 执行 Internal Synchronization 相关业务操作，并维护必要的状态和协作关系。
         */
        @Override
        public void afterCommit() {
            hook.afterCommit();
        }

        /**
         * 执行 Internal Synchronization 相关业务操作，并维护必要的状态和协作关系。
         *
         * @param status 用于完成本次业务处理的 status 参数。
         */
        @Override
        public void afterCompletion(int status) {
            if (status == STATUS_ROLLED_BACK) {
                hook.afterRollback();
            }
            hook.afterCompletion();
        }
    }
}
