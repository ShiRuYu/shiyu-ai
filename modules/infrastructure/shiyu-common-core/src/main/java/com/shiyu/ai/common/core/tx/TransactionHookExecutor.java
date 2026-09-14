package com.shiyu.ai.common.core.tx;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

/**
 * {@code TransactionHookExecutor} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
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

    /** 内部同步器（隔离 Spring SPI） */
    private static final class InternalSynchronization implements TransactionSynchronization {

        /**
         * hook 属性，保存当前对象中的业务数据或协作依赖。
         */
        private final TransactionHook hook;

        private InternalSynchronization(TransactionHook hook) {
            this.hook = hook;
        }

        /**
         * {@code beforeCommit} 执行当前类型定义的业务操作。
         *
         * @param readOnly 参数值，用于执行当前操作。
         */
        @Override
        public void beforeCommit(boolean readOnly) {
            hook.beforeCommit();
        }

        /**
         * {@code afterCommit} 执行当前类型定义的业务操作。
         */
        @Override
        public void afterCommit() {
            hook.afterCommit();
        }

        /**
         * {@code afterCompletion} 执行当前类型定义的业务操作。
         *
         * @param status 参数值，用于执行当前操作。
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
