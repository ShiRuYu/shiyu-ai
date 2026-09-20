package com.shiyu.ai.common.core.tx.event;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 处理 Transactional 事件 相关事件或请求，并推进后续业务流程。
 */
public abstract class TransactionalEventHandler<T extends DomainEvent> {

    /**
     * 执行 Transactional 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param BEFORE_COMMIT 用于完成本次业务处理的 BEFORE_COMMIT 参数。
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeCommit(T event) {}

    /**
     * 执行 Transactional 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param AFTER_COMMIT 用于完成本次业务处理的 AFTER_COMMIT 参数。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(T event) {}

    /**
     * 执行 Transactional 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param AFTER_ROLLBACK 用于完成本次业务处理的 AFTER_ROLLBACK 参数。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void afterRollback(T event) {}

    /**
     * 执行 Transactional 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param AFTER_COMPLETION 用于完成本次业务处理的 AFTER_COMPLETION 参数。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void afterCompletion(T event) {}
}
