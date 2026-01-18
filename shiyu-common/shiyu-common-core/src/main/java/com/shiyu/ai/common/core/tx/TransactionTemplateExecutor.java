package com.shiyu.ai.common.core.tx;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

public class TransactionTemplateExecutor {

    private final PlatformTransactionManager transactionManager;

    public TransactionTemplateExecutor(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public <T> T execute(TransactionDefinition definition, Supplier<T> action) {
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            T result = action.get();
            transactionManager.commit(status);
            return result;
        } catch (Throwable ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }

    /**
     * REQUIRES_NEW 语义
     */
    public <T> T executeNew(Supplier<T> action) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return execute(def, action);
    }
}

