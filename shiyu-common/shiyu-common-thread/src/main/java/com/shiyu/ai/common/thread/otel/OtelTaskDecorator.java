
package com.shiyu.ai.common.thread.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * OpenTelemetry任务装饰器
 * 用于在异步任务中传递OpenTelemetry上下文
 */
public class OtelTaskDecorator implements com.shiyu.ai.common.thread.api.TaskDecorator {

    private final Tracer tracer;

    /**
     * 创建OpenTelemetry任务装饰器
     * 
     * @param tracer OpenTelemetry追踪器
     */
    public OtelTaskDecorator(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        // 获取当前OpenTelemetry上下文
        Context otelContext = Context.current();

        return () -> {
            // 在新线程中恢复OpenTelemetry上下文
            try (Scope scope = otelContext.makeCurrent()) {
                // 创建子Span
                Span span = tracer.spanBuilder("async-task")
                    .setParent(otelContext)
                    .startSpan();

                try (Scope spanScope = span.makeCurrent()) {
                    runnable.run();
                } finally {
                    span.end();
                }
            }
        };
    }

    @Override
    public <V> Callable<V> decorate(Callable<V> callable) {
        // 获取当前OpenTelemetry上下文
        Context otelContext = Context.current();

        return () -> {
            // 在新线程中恢复OpenTelemetry上下文
            try (Scope scope = otelContext.makeCurrent()) {
                // 创建子Span
                Span span = tracer.spanBuilder("async-callable")
                    .setParent(otelContext)
                    .startSpan();

                try (Scope spanScope = span.makeCurrent()) {
                    return callable.call();
                } finally {
                    span.end();
                }
            }
        };
    }

    @Override
    public <T> Supplier<T> decorate(Supplier<T> supplier) {
        // 获取当前OpenTelemetry上下文
        Context otelContext = Context.current();

        return () -> {
            // 在新线程中恢复OpenTelemetry上下文
            try (Scope scope = otelContext.makeCurrent()) {
                // 创建子Span
                Span span = tracer.spanBuilder("async-supplier")
                    .setParent(otelContext)
                    .startSpan();

                try (Scope spanScope = span.makeCurrent()) {
                    return supplier.get();
                } finally {
                    span.end();
                }
            }
        };
    }
}
