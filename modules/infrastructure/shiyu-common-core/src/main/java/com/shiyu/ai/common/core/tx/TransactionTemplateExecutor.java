package com.shiyu.ai.common.core.tx;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

/**
 * 实现 Transaction Template Executor 相关的业务处理、协作逻辑或基础设施能力。
 */
public class TransactionTemplateExecutor {

    /**
     * transactionManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * 执行 Transaction Template Executor 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param transactionManager 用于完成本次业务处理的 transactionManager 参数。
     */
    public TransactionTemplateExecutor(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 调用 Transaction Template Executor 相关业务数据，并返回处理结果。
     *
     * @param definition 用于完成本次业务处理的 definition 参数。
     * @param action 用于完成本次业务处理的 action 参数。
     * @return 返回 Transaction Template Executor 相关操作生成的结果数据。
     */
    public <T> T execute(TransactionDefinition definition, Supplier<T> action) {
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            T result = action.get();
            transactionManager.commit(status);
            return result;
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }

    /** REQUIRES_NEW 语义 */
    public <T> T executeNew(Supplier<T> action) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return execute(def, action);
    }
}
