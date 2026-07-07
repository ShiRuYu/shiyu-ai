package com.shiyu.ai.knowledge.task;

import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 索引重建异步任务
 */
@Slf4j
@Component
public class IndexRebuildTask {

    private final KnowledgeSearchService knowledgeSearchService;
    private final Map<String, RebuildStatus> tasks = new ConcurrentHashMap<>();

    @Value("${shiyu.knowledge.indexing.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${shiyu.knowledge.indexing.retry.delay-ms:1000}")
    private long retryDelayMs;

    /** 任务超时时间（分钟），超过此时间仍未完成则标记为失败 */
    @Value("${shiyu.knowledge.indexing.task-timeout-minutes:30}")
    private int taskTimeoutMinutes;

    public IndexRebuildTask(KnowledgeSearchService knowledgeSearchService) {
        this.knowledgeSearchService = knowledgeSearchService;
    }

    /**
     * 创建任务
     */
    public String createTask() {
        String taskId = UUID.randomUUID().toString();
        RebuildStatus status = new RebuildStatus(taskId, "PENDING");
        status.setStartTime(LocalDateTime.now());
        tasks.put(taskId, status);
        return taskId;
    }

    /**
     * 提交异步重建任务
     */
    @Async("shiyuAsyncExecutor")
    public void submitRebuildTask(String taskId) {
        RebuildStatus status = tasks.get(taskId);
        if (status == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }

        status.setStatus("RUNNING");
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetryAttempts) {
            try {
                attempt++;
                status.setRetryCount(attempt);
                log.info("开始重建索引，尝试次数: {}/{}", attempt, maxRetryAttempts);

                int count = knowledgeSearchService.rebuildIndexWithProgress(progress -> {
                    status.setProgress(progress);
                });

                status.setStatus("COMPLETED");
                status.setTotal(count);
                status.setIndexed(count);
                status.setProgress(100);
                status.setEndTime(LocalDateTime.now());
                log.info("索引重建完成: taskId={}, count={}", taskId, count);
                return;

            } catch (Exception e) {
                lastException = e;
                log.error("索引重建失败，尝试次数: {}/{}, error: {}", attempt, maxRetryAttempts, e.getMessage());

                if (attempt < maxRetryAttempts) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        status.setStatus("FAILED");
                        status.setError("任务被中断");
                        status.setEndTime(LocalDateTime.now());
                        return;
                    }
                }
            }
        }

        // 所有重试都失败
        status.setStatus("FAILED");
        status.setError(lastException != null ? lastException.getMessage() : "未知错误");
        status.setEndTime(LocalDateTime.now());
        log.error("索引重建最终失败: taskId={}, attempts={}", taskId, attempt);
    }

    /**
     * 定时清理超时任务（每 5 分钟执行一次）
     * 兜底：如果 @Async 因配置问题未执行，任务将永远停在 PENDING，此处将其标记为失败
     */
    @Scheduled(fixedRateString = "${shiyu.knowledge.indexing.cleanup-interval-ms:300000}")
    public void timeoutStuckTasks() {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, RebuildStatus> entry : tasks.entrySet()) {
            RebuildStatus st = entry.getValue();
            if ("PENDING".equals(st.getStatus()) || "RUNNING".equals(st.getStatus())) {
                if (st.getStartTime() != null) {
                    long elapsed = ChronoUnit.MINUTES.between(st.getStartTime(), now);
                    if (elapsed >= taskTimeoutMinutes) {
                        log.warn("任务超时，标记为失败: taskId={}, status={}, elapsed={}min",
                                entry.getKey(), st.getStatus(), elapsed);
                        st.setStatus("FAILED");
                        st.setError("任务超时（超过 " + taskTimeoutMinutes + " 分钟）");
                        st.setEndTime(now);
                    }
                }
            }
        }
    }

    /**
     * 获取任务状态
     */
    public RebuildStatus getTaskStatus(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * 获取所有任务
     */
    public List<RebuildStatus> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * 清理已完成的任务（保留最近 100 个）
     */
    public void cleanupCompletedTasks() {
        if (tasks.size() <= 100) {
            return;
        }

        tasks.entrySet().removeIf(entry -> {
            RebuildStatus status = entry.getValue();
            return "COMPLETED".equals(status.getStatus()) || "FAILED".equals(status.getStatus());
        });

        log.info("清理已完成的任务，当前任务数: {}", tasks.size());
    }
}
