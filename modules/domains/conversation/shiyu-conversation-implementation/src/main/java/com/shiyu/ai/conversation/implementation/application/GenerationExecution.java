package com.shiyu.ai.conversation.implementation.application;

import reactor.core.Disposable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** 管理单次生成的取消句柄与配额心跳，协调取消先于订阅建立的竞态。 */
final class GenerationExecution {
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicReference<Disposable> subscription = new AtomicReference<>();
    private final AtomicReference<ScheduledFuture<?>> reservationHeartbeat = new AtomicReference<>();

    void cancel() {
        cancelled.set(true);
        stopReservationHeartbeat();
        Disposable current = subscription.get();
        if (current != null) current.dispose();
    }

    void attachReservationHeartbeat(ScheduledFuture<?> heartbeat) {
        if (heartbeat == null) return;
        reservationHeartbeat.set(heartbeat);
        if (cancelled.get()) heartbeat.cancel(false);
    }

    void stopReservationHeartbeat() {
        ScheduledFuture<?> heartbeat = reservationHeartbeat.getAndSet(null);
        if (heartbeat != null) heartbeat.cancel(false);
    }

    void attach(Disposable value) {
        subscription.set(value);
        if (cancelled.get()) value.dispose();
    }

    boolean isCancelled() {
        return cancelled.get();
    }
}
