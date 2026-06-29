package com.shiyu.ai.aiagent.node;

import com.shiyu.ai.aiagent.service.ExecutionHistoryService;
import com.shiyu.ai.common.core.utils.UnifiedThreadPoolUtils;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

@Setter
@Getter
@Slf4j
public abstract class BaseNode implements NodeAction<AgentState> {

    protected NodeConfig config;

    protected ExecutionHistoryService executionHistoryService;

    public BaseNode() {
        this.config = NodeConfig.builder().build();
    }

    public BaseNode(NodeConfig config) {
        this.config = config;
    }

    public Map<String, Object> apply(AgentState state) throws Exception {
        long startTime = System.currentTimeMillis();
        String executionId = null;

        try {
            beforeExecute(state);

            NodeInput input = processParameters(state);

            int retries = Optional.ofNullable(config.getRetryCount()).orElse(0);
            long timeoutMs = Optional.ofNullable(config.getTimeout()).orElse(30000L);
            long retryIntervalMs = Optional.ofNullable(config.getRetryInterval()).orElse(1000L);

            if (executionHistoryService != null) {
                Map<String, Object> data = state.data();
                executionId = executionHistoryService.startExecution(
                        getStr(data, NodeFields.FieldKey.AGENT_ID),
                        getStr(data, NodeFields.FieldKey.VERSION),
                        getLong(data, NodeFields.FieldKey.USER_ID),
                        getStr(data, NodeFields.FieldKey.SESSION_ID),
                        config.getNodeId(),
                        config.getNodeType() != null ? config.getNodeType().getCode() : null,
                        JSONUtils.toJsonString(input.toMap())
                );
            }

            Exception lastError = null;

            for (int attempt = 0; attempt <= retries; attempt++) {
                if (attempt > 0) {
                    log.warn("节点 [{}] 第 {}/{} 次重试", config.getNodeName(), attempt, retries);
                    Thread.sleep(retryIntervalMs);
                }
                try {
                    NodeOutput output = executeWithTimeout(input, timeoutMs);
                    afterExecute(state, output);

                    if (executionHistoryService != null && executionId != null) {
                        executionHistoryService.completeExecution(executionId,
                                JSONUtils.toJsonString(output.toMap()), "SUCCESS", null);
                    }
                    return output.toMap();

                } catch (Exception e) {
                    lastError = e;
                    log.warn("节点 [{}] 执行失败 (尝试 {}/{})", config.getNodeName(), attempt, retries, e.getMessage());
                }
            }

            Map<String, Object> fallback = handleException(state, lastError);
            if (executionHistoryService != null && executionId != null) {
                executionHistoryService.completeExecution(executionId,
                        JSONUtils.toJsonString(fallback), "FAILED",
                        lastError != null ? lastError.getMessage() : "未知错误");
            }
            return fallback;

        } catch (Exception e) {
            log.error("节点执行失败: {}", config.getNodeName(), e);
            if (executionHistoryService != null && executionId != null) {
                executionHistoryService.completeExecution(executionId, null, "FAILED", e.getMessage());
            }
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 1000) {
                log.info("节点 [{}] 执行耗时: {}ms", config.getNodeName(), duration);
            }
        }
    }

    private NodeOutput executeWithTimeout(NodeInput input, long timeoutMs) throws Exception {
        if (timeoutMs <= 0) {
            return doExecute(input);
        }
        // 使用 shiyu-common UnifiedThreadPoolUtils 共享线程池，避免每次创建/销毁的开销
        ExecutorService executor = UnifiedThreadPoolUtils.getNamedExecutor("node-exec");
        Future<NodeOutput> future = executor.submit(() -> doExecute(input));
        try {
            return future.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new Exception("节点执行超时 (" + timeoutMs + "ms): " + config.getNodeName());
        }
    }

    protected void beforeExecute(AgentState state) {
        log.info("开始执行节点: {}", config.getNodeName());
        if ("DEBUG".equalsIgnoreCase(config.getLogLevel())) {
            log.debug("节点配置: {}", config);
        }
    }

    protected NodeInput processParameters(AgentState state) {
        log.debug("处理节点参数: {}", config.getNodeName());
        return NodeInput.fromMap(state.data());
    }

    protected abstract NodeOutput doExecute(NodeInput input) throws Exception;

    protected void afterExecute(AgentState state, NodeOutput output) {
        log.info("节点执行完成: {}", config.getNodeName());
    }

    protected Map<String, Object> handleException(AgentState state, Exception e) {
        String errorStrategy = config.getErrorStrategy();

        switch (errorStrategy) {
            case "IGNORE":
                log.warn("忽略异常，继续执行: {}", e.getMessage());
                return Collections.emptyMap();

            case "DEFAULT":
                log.warn("使用默认值处理异常: {}", e.getMessage());
                return createDefaultResult();

            case "THROW":
            default:
                log.error("抛出异常: {}", e.getMessage(), e);
                throw new RuntimeException("节点执行失败: " + config.getNodeName(), e);
        }
    }

    protected Map<String, Object> createDefaultResult() {
        return Map.of(
            NodeFields.FieldKey.ERROR.key(), "使用默认值处理",
            "status", "DEFAULT_APPLIED"
        );
    }

    private static String getStr(Map<String, Object> data, NodeFields.FieldKey key) {
        Object v = data.get(key.key());
        return v != null ? v.toString() : null;
    }

    private static Long getLong(Map<String, Object> data, NodeFields.FieldKey key) {
        Object v = data.get(key.key());
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
