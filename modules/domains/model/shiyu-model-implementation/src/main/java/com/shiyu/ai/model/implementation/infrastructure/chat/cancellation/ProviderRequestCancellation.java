package com.shiyu.ai.model.implementation.infrastructure.chat.cancellation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Tracks provider HTTP requests started during one streaming model invocation. */
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

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }

    @FunctionalInterface
    public interface Registration extends AutoCloseable {
        @Override
        void close();
    }
}
