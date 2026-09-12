package com.shiyu.ai.common.core.tx;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

/**
 * {@code TransactionTemplateExecutor} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public class TransactionTemplateExecutor {

    /**
     * transactionManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * {@code TransactionTemplateExecutor} 创建并初始化当前类型实例。
     *
     * @param transactionManager 参数值，用于执行当前操作。
     */
    public TransactionTemplateExecutor(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * {@code execute} 执行当前模块定义的业务流程。
     *
     * @param definition 参数值，用于执行当前操作。
     * @param action 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
