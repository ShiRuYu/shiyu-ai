package com.shiyu.ai.common.core.tx;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

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
                new InternalSynchronization(hook)
        );
    }

    /**
     * 默认：无事务不执行（严格模式）
     */
    public static void register(TransactionHook hook) {
        register(hook, false);
    }

    /**
     * 内部同步器（隔离 Spring SPI）
     */
    private static final class InternalSynchronization
            implements TransactionSynchronization {

        private final TransactionHook hook;

        private InternalSynchronization(TransactionHook hook) {
            this.hook = hook;
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            hook.beforeCommit();
        }

        @Override
        public void afterCommit() {
            hook.afterCommit();
        }

        @Override
        public void afterCompletion(int status) {
            if (status == STATUS_ROLLED_BACK) {
                hook.afterRollback();
            }
            hook.afterCompletion();
        }
    }
}

