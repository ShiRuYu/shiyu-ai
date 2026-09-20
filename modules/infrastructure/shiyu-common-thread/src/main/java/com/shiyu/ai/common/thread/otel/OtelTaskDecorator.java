package com.shiyu.ai.common.thread.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 实现 Otel Task Decorator 相关的业务处理、协作逻辑或基础设施能力。
 */
@SuppressWarnings("try")
public class OtelTaskDecorator implements com.shiyu.ai.common.thread.api.TaskDecorator {

    /**
     * tracer 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Tracer tracer;

    /**
     * 创建OpenTelemetry任务装饰器
     *
     * @param tracer OpenTelemetry追踪器
     */
    public OtelTaskDecorator(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * 执行 Otel Task Decorator 相关业务数据，并返回处理结果。
     *
     * @param runnable 用于完成本次业务处理的 runnable 参数。
     * @return 返回 Otel Task Decorator 相关操作生成的结果数据。
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        // 获取当前OpenTelemetry上下文
        Context otelContext = Context.current();

        return () -> {
            // 在新线程中恢复OpenTelemetry上下文
            try (Scope scope = otelContext.makeCurrent()) {
                // 创建子Span
                Span span = tracer.spanBuilder("async-task").setParent(otelContext).startSpan();

                try (Scope spanScope = span.makeCurrent()) {
                    runnable.run();
                } finally {
                    span.end();
                }
            }
        };
    }

    /**
     * 执行 Otel Task Decorator 相关业务数据，并返回处理结果。
     *
     * @param callable 用于完成本次业务处理的 callable 参数。
     * @return 返回 Otel Task Decorator 相关操作生成的结果数据。
     */
    @Override
    public <V> Callable<V> decorate(Callable<V> callable) {
        // 获取当前OpenTelemetry上下文
        Context otelContext = Context.current();

        return () -> {
            // 在新线程中恢复OpenTelemetry上下文
            try (Scope scope = otelContext.makeCurrent()) {
                // 创建子Span
                Span span = tracer.spanBuilder("async-callable").setParent(otelContext).startSpan();

                try (Scope spanScope = span.makeCurrent()) {
                    return callable.call();
                } finally {
                    span.end();
                }
            }
        };
    }

    /**
     * 执行 Otel Task Decorator 相关业务数据，并返回处理结果。
     *
     * @param supplier 用于完成本次业务处理的 supplier 参数。
     * @return 返回 Otel Task Decorator 相关操作生成的结果数据。
     */
    @Override
    public <T> Supplier<T> decorate(Supplier<T> supplier) {
        // 获取当前OpenTelemetry上下文
        Context otelContext = Context.current();

        return () -> {
            // 在新线程中恢复OpenTelemetry上下文
            try (Scope scope = otelContext.makeCurrent()) {
                // 创建子Span
                Span span = tracer.spanBuilder("async-supplier").setParent(otelContext).startSpan();

                try (Scope spanScope = span.makeCurrent()) {
                    return supplier.get();
                } finally {
                    span.end();
                }
            }
        };
    }
}
