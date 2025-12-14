
package com.shiyu.ai.common.thread.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务上下文接口
 * 用于在线程间传递上下文信息，如用户信息、链路追踪ID等
 */
public interface TaskContext {

    /**
     * 获取当前上下文
     * 
     * @return 当前上下文实例
     */
    static TaskContext current() {
        return TaskContextHolder.get();
    }

    /**
     * 设置当前上下文
     * 
     * @param context 上下文实例
     */
    static void setCurrent(TaskContext context) {
        TaskContextHolder.set(context);
    }

    /**
     * 清除当前上下文
     */
    static void clear() {
        TaskContextHolder.clear();
    }

    /**
     * 获取上下文属性
     * 
     * @param key 属性键
     * @return 属性值
     */
    Object get(String key);

    /**
     * 设置上下文属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    void set(String key, Object value);

    /**
     * 移除上下文属性
     * 
     * @param key 属性键
     * @return 被移除的属性值
     */
    Object remove(String key);

    /**
     * 获取所有上下文属性
     * 
     * @return 属性映射
     */
    Map<String, Object> getAll();

    /**
     * 创建当前上下文的快照
     * 
     * @return 上下文快照
     */
    TaskContext snapshot();

    /**
     * 从快照恢复上下文
     * 
     * @param snapshot 上下文快照
     */
    void restore(TaskContext snapshot);

    /**
     * 上下文持有者，用于线程本地存储
     */
    class TaskContextHolder {
        private static final ThreadLocal<TaskContext> CONTEXT_HOLDER = new ThreadLocal<>();

        static TaskContext get() {
            TaskContext context = CONTEXT_HOLDER.get();
            if (context == null) {
                context = new DefaultTaskContext();
                CONTEXT_HOLDER.set(context);
            }
            return context;
        }

        static void set(TaskContext context) {
            CONTEXT_HOLDER.set(context);
        }

        static void clear() {
            CONTEXT_HOLDER.remove();
        }
    }

    /**
     * 默认任务上下文实现
     */
    class DefaultTaskContext implements TaskContext {
        private final Map<String, Object> context = new ConcurrentHashMap<>();

        @Override
        public Object get(String key) {
            return context.get(key);
        }

        @Override
        public void set(String key, Object value) {
            context.put(key, value);
        }

        @Override
        public Object remove(String key) {
            return context.remove(key);
        }

        @Override
        public Map<String, Object> getAll() {
            return new ConcurrentHashMap<>(context);
        }

        @Override
        public TaskContext snapshot() {
            DefaultTaskContext snapshot = new DefaultTaskContext();
            snapshot.context.putAll(this.context);
            return snapshot;
        }

        @Override
        public void restore(TaskContext snapshot) {
            if (snapshot instanceof DefaultTaskContext) {
                this.context.clear();
                this.context.putAll(((DefaultTaskContext) snapshot).context);
            }
        }
    }
}
