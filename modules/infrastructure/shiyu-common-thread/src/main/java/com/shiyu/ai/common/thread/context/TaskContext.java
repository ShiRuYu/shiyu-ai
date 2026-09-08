package com.shiyu.ai.common.thread.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 任务上下文
 * 用于在线程间传递上下文信息
 */
public class TaskContext {

    private static final ThreadLocal<TaskContext> CONTEXT_HOLDER = ThreadLocal.withInitial(TaskContext::new);

    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * 获取当前线程的上下文
     *
     * @return 当前上下文
     */
    public static TaskContext current() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的上下文
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 设置属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 获取属性
     *
     * @param key 属性键
     * @param <T> 属性类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 获取属性，如果不存在则返回默认值
     *
     * @param key 属性键
     * @param defaultValue 默认值
     * @param <T> 属性类型
     * @return 属性值
     */
    public <T> T getAttribute(String key, T defaultValue) {
        return Optional.<T>ofNullable(getAttribute(key)).orElse(defaultValue);
    }

    /**
     * 移除属性
     *
     * @param key 属性键
     * @param <T> 属性类型
     * @return 被移除的属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T removeAttribute(String key) {
        return (T) attributes.remove(key);
    }

    /**
     * 创建当前上下文的快照
     *
     * @return 上下文快照
     */
    public TaskContext snapshot() {
        TaskContext snapshot = new TaskContext();
        snapshot.attributes.putAll(this.attributes);
        return snapshot;
    }

    /**
     * 从快照恢复上下文
     *
     * @param snapshot 上下文快照
     */
    public void restore(TaskContext snapshot) {
        if (snapshot != null) {
            this.attributes.clear();
            this.attributes.putAll(snapshot.attributes);
        }
    }

    /**
     * 清空所有属性
     */
    public void clearAttributes() {
        attributes.clear();
    }

    /**
     * 获取所有属性
     *
     * @return 属性映射
     */
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
}