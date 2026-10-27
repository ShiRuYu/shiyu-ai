package com.shiyu.ai.model.implementation.infrastructure.chat.cancellation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** 跟踪一次模型流式调用期间发起的提供方 HTTP 请求，支持统一取消。 */
public final class ProviderRequestCancellation {
    private static final ThreadLocal<ProviderRequestCancellation> CURRENT = new ThreadLocal<>();

    private final Set<Runnable> activeRequests = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public static Scope enter(ProviderRequestCancellation cancellation) {
        ProviderRequestCancellation previous = CURRENT.get();
        CURRENT.set(cancellation);
        return () -> {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        };
    }

    public static Registration registerCurrent(Runnable cancelRequest) {
        ProviderRequestCancellation current = CURRENT.get();
        return current == null ? () -> {} : current.register(cancelRequest);
    }

    public void cancel() {
        if (!cancelled.compareAndSet(false, true)) return;
        for (Runnable request : activeRequests) {
            if (activeRequests.remove(request)) request.run();
        }
    }

    private Registration register(Runnable cancelRequest) {
        activeRequests.add(cancelRequest);
        if (cancelled.get() && activeRequests.remove(cancelRequest)) cancelRequest.run();
        return () -> activeRequests.remove(cancelRequest);
    }

    /** 表示当前线程中的提供方请求取消作用域，关闭时恢复先前上下文。 */
    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        /**
         * 关闭当前取消作用域，恢复进入前的线程上下文。
         */
        @Override
        void close();
    }

    /** 表示取消回调的注册句柄，关闭时解除对应回调。 */
    @FunctionalInterface
    public interface Registration extends AutoCloseable {
        /**
         * 解除取消回调注册，使其不再接收后续取消通知。
         */
        @Override
        void close();
    }
}
