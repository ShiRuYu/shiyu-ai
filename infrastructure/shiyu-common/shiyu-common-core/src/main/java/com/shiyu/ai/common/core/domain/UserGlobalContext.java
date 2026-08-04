package com.shiyu.ai.common.core.domain;

/**
 * 登录用户全局上下文持有者
 * <p>
 * 使用 InheritableThreadLocal 确保子线程可以自动继承父线程的登录上下文。
 * 适用于 Reactor 调度、@Async 异步方法等线程池场景。
 */
public class UserGlobalContext {
    private static final ThreadLocal<UserContext> USER_HOLDER = new InheritableThreadLocal<>();

    public static void set(UserContext user) {
        USER_HOLDER.set(user);
    }

    public static UserContext get() {
        return USER_HOLDER.get();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
