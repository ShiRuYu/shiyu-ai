package com.shiyu.ai.agent.implementation.runtime.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 跟踪图执行工作线程处理显式流取消时的状态。 */
public final class AgentCancellationLogContext {

    private static final Set<Long> CANCELLED_THREAD_IDS = ConcurrentHashMap.newKeySet();

    private AgentCancellationLogContext() {}

    /** 在中断工作线程前写入标记，供日志过滤器识别取消控制流。 */
    public static void mark(Thread worker) {
        if (worker != null) CANCELLED_THREAD_IDS.add(worker.threadId());
    }

    /** 工作线程结束取消退出后，移除其标记。 */
    public static void clear(Thread worker) {
        if (worker != null) CANCELLED_THREAD_IDS.remove(worker.threadId());
    }

    /** 判断日志事件所属线程是否正在退出用户请求的取消流程。 */
    public static boolean isMarked(long threadId) {
        return CANCELLED_THREAD_IDS.contains(threadId);
    }
}
