
package com.shiyu.ai.common.thread.otel;

import com.shiyu.ai.common.thread.api.TaskContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;

/**
 * OpenTelemetry上下文桥接器
 * 用于在TaskContext和OpenTelemetry Context之间进行转换和同步
 */
public class OtelContextBridge {

    private static final String TRACE_ID_KEY = "otel.traceId";
    private static final String SPAN_ID_KEY = "otel.spanId";
    private static final String TRACE_FLAGS_KEY = "otel.traceFlags";
    private static final String TRACE_STATE_KEY = "otel.traceState";

    /**
     * 从OpenTelemetry上下文提取信息到TaskContext
     * 
     * @param taskContext 任务上下文
     */
    public static void extractFromOtel(TaskContext taskContext) {
        Context otelContext = Context.current();
        Span span = Span.fromContextOrNull(otelContext);

        if (span != null) {
            SpanContext spanContext = span.getSpanContext();

            // 提取追踪信息
            taskContext.set(TRACE_ID_KEY, spanContext.getTraceId());
            taskContext.set(SPAN_ID_KEY, spanContext.getSpanId());
            taskContext.set(TRACE_FLAGS_KEY, spanContext.getTraceFlags());
            taskContext.set(TRACE_STATE_KEY, spanContext.getTraceState());
        }
    }

    /**
     * 从TaskContext恢复OpenTelemetry上下文
     * 
     * @param taskContext 任务上下文
     * @return OpenTelemetry上下文
     */
    public static Context restoreToOtel(TaskContext taskContext) {
        Object traceId = taskContext.get(TRACE_ID_KEY);
        Object spanId = taskContext.get(SPAN_ID_KEY);
        Object traceFlags = taskContext.get(TRACE_FLAGS_KEY);
        Object traceState = taskContext.get(TRACE_STATE_KEY);

        if (traceId != null && spanId != null) {
            // 使用反射创建SpanContext，避免API版本兼容性问题
            SpanContext spanContext;
            try {
                // 尝试使用新版本的API
                java.lang.reflect.Method createFromRemoteMethod = SpanContext.class.getMethod(
                    "createFromRemote", 
                    String.class, 
                    String.class, 
                    TraceFlags.class, 
                    TraceState.class
                );
                spanContext = (SpanContext) createFromRemoteMethod.invoke(
                    null,
                    traceId.toString(),
                    spanId.toString(),
                    traceFlags instanceof TraceFlags ? (TraceFlags) traceFlags : TraceFlags.getDefault(),
                    traceState instanceof TraceState ? (TraceState) traceState : TraceState.getDefault()
                );
            } catch (Exception e) {
                // 如果新版本API不可用，尝试使用旧版本API
                try {
                    java.lang.reflect.Method createFromRemoteMethod = SpanContext.class.getMethod(
                        "createFromRemote", 
                        String.class, 
                        String.class, 
                        TraceFlags.class, 
                        TraceState.class,
                        boolean.class
                    );
                    spanContext = (SpanContext) createFromRemoteMethod.invoke(
                        null,
                        traceId.toString(),
                        spanId.toString(),
                        traceFlags instanceof TraceFlags ? (TraceFlags) traceFlags : TraceFlags.getDefault(),
                        traceState instanceof TraceState ? (TraceState) traceState : TraceState.getDefault(),
                        false
                    );
                } catch (Exception ex) {
                    // 如果所有方法都不可用，创建一个空的上下文
                    spanContext = SpanContext.getInvalid();
                }
            }

            // 创建带有SpanContext的Context
            return Context.root().with(Span.wrap(spanContext));
        }

        return Context.root();
    }

    /**
     * 同步两个上下文
     * 
     * @param taskContext 任务上下文
     */
    public static void syncContexts(TaskContext taskContext) {
        // 从OpenTelemetry提取到TaskContext
        extractFromOtel(taskContext);
    }

    /**
     * 检查当前OpenTelemetry上下文是否有效
     * 
     * @return 是否有效
     */
    public static boolean isOtelContextValid() {
        Context otelContext = Context.current();
        Span span = Span.fromContextOrNull(otelContext);
        return span != null && span.getSpanContext().isValid();
    }
}
