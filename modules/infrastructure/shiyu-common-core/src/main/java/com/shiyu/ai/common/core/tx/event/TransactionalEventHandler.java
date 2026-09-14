package com.shiyu.ai.common.core.tx.event;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * {@code TransactionalEventHandler} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public abstract class TransactionalEventHandler<T extends DomainEvent> {

    /**
     * {@code beforeCommit} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeCommit(T event) {}

    /**
     * {@code afterCommit} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(T event) {}

    /**
     * {@code afterRollback} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void afterRollback(T event) {}

    /**
     * {@code afterCompletion} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void afterCompletion(T event) {}
}
