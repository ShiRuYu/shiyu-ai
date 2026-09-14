package com.shiyu.ai.common.core.domain;

/**
 * 登录用户全局上下文持有者
 *
 * <p>使用 InheritableThreadLocal 确保子线程可以自动继承父线程的登录上下文。 适用于 Reactor 调度、@Async 异步方法等线程池场景。
 */
public class UserGlobalContext {
    private static final ThreadLocal<UserContext> USER_HOLDER = new InheritableThreadLocal<>();

    /**
     * {@code set} 写入或更新当前模块中的业务数据。
     *
     * @param user 参数值，用于执行当前操作。
     */
    public static void set(UserContext user) {
        USER_HOLDER.set(user);
    }

    /**
     * {@code get} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static UserContext get() {
        return USER_HOLDER.get();
    }

    /**
     * {@code clear} 执行当前类型定义的业务操作。
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
