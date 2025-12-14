
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.TaskContext;

/**
 * 上下文感知的Runnable包装器
 * 确保任务执行时能够正确传递上下文信息
 */
public class ContextAwareRunnable implements Runnable {

    private final Runnable delegate;
    private final TaskContext contextSnapshot;

    /**
     * 创建上下文感知的Runnable
     * 
     * @param delegate 原始Runnable
     */
    public ContextAwareRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.contextSnapshot = TaskContext.current().snapshot();
    }

    /**
     * 创建上下文感知的Runnable，使用指定的上下文快照
     * 
     * @param delegate 原始Runnable
     * @param contextSnapshot 上下文快照
     */
    public ContextAwareRunnable(Runnable delegate, TaskContext contextSnapshot) {
        this.delegate = delegate;
        this.contextSnapshot = contextSnapshot;
    }

    @Override
    public void run() {
        TaskContext originalContext = TaskContext.current();
        try {
            // 恢复上下文
            TaskContext.current().restore(contextSnapshot);
            // 执行任务
            delegate.run();
        } finally {
            // 恢复原始上下文
            TaskContext.current().restore(originalContext);
        }
    }

    /**
     * 获取原始Runnable
     * 
     * @return 原始Runnable
     */
    public Runnable getDelegate() {
        return delegate;
    }

    /**
     * 获取上下文快照
     * 
     * @return 上下文快照
     */
    public TaskContext getContextSnapshot() {
        return contextSnapshot;
    }
}
