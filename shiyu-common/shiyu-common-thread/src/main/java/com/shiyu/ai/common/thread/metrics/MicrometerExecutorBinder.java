
package com.shiyu.ai.common.thread.metrics;

import com.shiyu.ai.common.thread.api.TaskDecorator;
import com.shiyu.ai.common.thread.context.ContextAwareCallable;
import com.shiyu.ai.common.thread.context.ContextAwareRunnable;
import com.shiyu.ai.common.thread.context.TaskContext;
import com.shiyu.ai.common.thread.core.SafeExecutorService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

/**
 * Micrometer执行器绑定器
 * 用于收集线程池的指标数据
 */
public class MicrometerExecutorBinder implements MeterBinder {

    private static final Logger logger = LoggerFactory.getLogger(MicrometerExecutorBinder.class);

    private final Executor executor;
    private final String name;

    /**
     * 创建Micrometer执行器绑定器
     * 
     * @param executor 线程池执行器
     * @param name 线程池名称
     */
    public MicrometerExecutorBinder(Executor executor, String name) {
        this.executor = executor;
        this.name = name;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Executor targetExecutor = executor;
        // 如果是SafeExecutorService包装器，则尝试获取其委托的执行器
        if (executor instanceof SafeExecutorService safeExecutorService) {
            targetExecutor = safeExecutorService.getDelegate();
        }
        if (targetExecutor instanceof ThreadPoolExecutor threadPoolExecutor) {
            bindThreadPoolExecutor(threadPoolExecutor, registry);
        } else if (targetExecutor instanceof ExecutorService executorService) {
            bindExecutorService(executorService, registry);
        } else {
            logger.warn("不支持的执行器类型: {}, 无法绑定指标", targetExecutor.getClass().getName());
        }
    }

    /**
     * 绑定ThreadPoolExecutor的指标
     * 
     * @param executor 线程池执行器
     * @param registry 指标注册表
     */
    private void bindThreadPoolExecutor(ThreadPoolExecutor executor, MeterRegistry registry) {
        String prefix = "executor." + name;

        // 核心线程数
        Gauge.builder(prefix + ".core.pool.size", executor, ThreadPoolExecutor::getCorePoolSize)
            .description("线程池核心线程数")
            .register(registry);

        // 最大线程数
        Gauge.builder(prefix + ".max.pool.size", executor, ThreadPoolExecutor::getMaximumPoolSize)
            .description("线程池最大线程数")
            .register(registry);

        // 活跃线程数
        Gauge.builder(prefix + ".active.count", executor, ThreadPoolExecutor::getActiveCount)
            .description("线程池活跃线程数")
            .register(registry);

        // 池大小
        Gauge.builder(prefix + ".pool.size", executor, ThreadPoolExecutor::getPoolSize)
            .description("线程池当前线程数")
            .register(registry);

        // 队列大小
        Gauge.builder(prefix + ".queue.size", executor, e -> e.getQueue().size())
            .description("线程池队列大小")
            .register(registry);

        // 队列剩余容量
        Gauge.builder(prefix + ".queue.remaining.capacity", executor, e -> e.getQueue().remainingCapacity())
            .description("线程池队列剩余容量")
            .register(registry);

        // 已完成任务数
        Gauge.builder(prefix + ".completed.task.count", executor, ThreadPoolExecutor::getCompletedTaskCount)
            .description("线程池已完成的任务数")
            .register(registry);

        // 总任务数
        Gauge.builder(prefix + ".task.count", executor, e -> e.getCompletedTaskCount() + e.getActiveCount())
            .description("线程池总任务数")
            .register(registry);
    }

    /**
     * 绑定ExecutorService的指标
     * 
     * @param executor 执行器服务
     * @param registry 指标注册表
     */
    private void bindExecutorService(ExecutorService executor, MeterRegistry registry) {
        String prefix = "executor." + name;

        // 是否已关闭
        Gauge.builder(prefix + ".terminated", executor, e -> e.isTerminated() ? 1 : 0)
            .description("执行器是否已终止")
            .register(registry);

        // 是否已关闭
        Gauge.builder(prefix + ".shutdown", executor, e -> e.isShutdown() ? 1 : 0)
            .description("执行器是否已关闭")
            .register(registry);
    }

    /**
     * 创建任务执行计时器
     * 
     * @param registry 指标注册表
     * @param name 计时器名称
     * @return 计时器
     */
    public static Timer createTaskTimer(MeterRegistry registry, String name) {
        return Timer.builder("executor.task.execution")
            .description("任务执行时间")
            .tag("name", name)
            .register(registry);
    }

    /**
     * 创建任务提交计时器
     * 
     * @param registry 指标注册表
     * @param name 计时器名称
     * @return 计时器
     */
    public static Timer createSubmissionTimer(MeterRegistry registry, String name) {
        return Timer.builder("executor.task.submission")
            .description("任务提交时间")
            .tag("name", name)
            .register(registry);
    }

    /**
     * 包装执行器以收集指标
     * 
     * @param executor 原始执行器
     * @param name 执行器名称
     * @param registry 指标注册表
     * @return 带指标收集的执行器
     */
    public static Executor wrapWithMetrics(Executor executor, String name, MeterRegistry registry) {
        Timer taskTimer = createTaskTimer(registry, name);
        Timer submissionTimer = createSubmissionTimer(registry, name);

        return task -> {
            // 记录任务提交时间
            submissionTimer.record(() -> {
                // 执行任务并记录执行时间
                taskTimer.record(() -> executor.execute(task));
            });
        };
    }
}
