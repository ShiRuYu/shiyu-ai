package com.shiyu.ai.agent.contract.node;

import com.shiyu.ai.agent.contract.ExecutionHistoryService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * 提供 Agent 节点执行的通用模板、上下文传递和执行历史记录。
 */
@Setter
@Getter
@Slf4j
public abstract class BaseNode {

    /**
     * 配置，表示当前对象中的对应属性。
     */
    protected NodeConfig config;

    /**
     * executionHistoryService 属性，保存当前对象中的业务数据或协作依赖。
     */
    protected ExecutionHistoryService executionHistoryService;

    /**
     * {@code BaseNode} 创建并初始化当前类型实例。
     */
    public BaseNode() {
        this.config = NodeConfig.builder().build();
    }

    /**
     * {@code BaseNode} 创建并初始化当前类型实例。
     *
     * @param config 参数值，用于执行当前操作。
     */
    public BaseNode(NodeConfig config) {
        this.config = config;
    }

    /**
     * {@code apply} 执行当前类型定义的业务操作。
     *
     * @param stateData 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> apply(Map<String, Object> stateData) throws Exception {
        stateData = stateData == null ? Map.of() : stateData;
        long startTime = System.currentTimeMillis();
        String executionId = null;
        ActorContext actor = null;

        try {
            beforeExecute(stateData);

            NodeInput input = processParameters(stateData);

            int retries = Optional.ofNullable(config.getRetryCount()).orElse(0);
            long timeoutMs = Optional.ofNullable(config.getTimeout()).orElse(30000L);
            long retryIntervalMs = Optional.ofNullable(config.getRetryInterval()).orElse(1000L);

            if (executionHistoryService != null) {
                actor = actor(stateData);
                executionId =
                        executionHistoryService.startExecution(
                                actor,
                                getStr(stateData, NodeFields.FieldKey.AGENT_ID),
                                getStr(stateData, NodeFields.FieldKey.VERSION),
                                getStr(stateData, NodeFields.FieldKey.SESSION_ID),
                                config.getNodeId(),
                                config.getNodeType() != null
                                        ? config.getNodeType().getCode()
                                        : null,
                                String.valueOf(input.toMap()));
            }

            Exception lastError = null;

            for (int attempt = 0; attempt <= retries; attempt++) {
                if (attempt > 0) {
                    log.warn(
                            "节点重试：nodeNamePresent={}, attempt={}/{}",
                            config.getNodeName() != null,
                            attempt,
                            retries);
                    Thread.sleep(retryIntervalMs);
                }
                try {
                    NodeOutput output = executeWithTimeout(input, timeoutMs);
                    afterExecute(stateData, output);

                    if (executionHistoryService != null && executionId != null) {
                        executionHistoryService.completeExecution(
                                actor, executionId, String.valueOf(output.toMap()), 1, null);
                    }
                    return output.toMap();

                } catch (Exception e) {
                    lastError = e;
                    log.warn(
                            "节点执行失败: nodeNameLength={}, attempt={}/{}, errorType={},"
                                    + " errorMessageLength={}",
                            valueLength(config.getNodeName()),
                            attempt,
                            retries,
                            e.getClass().getSimpleName(),
                            valueLength(e.getMessage()));
                }
            }

            Map<String, Object> fallback = handleException(stateData, lastError);
            if (executionHistoryService != null && executionId != null) {
                executionHistoryService.completeExecution(
                        actor,
                        executionId,
                        String.valueOf(fallback),
                        2,
                        lastError != null ? lastError.getMessage() : "未知错误");
            }
            return fallback;

        } catch (Exception e) {
            log.error(
                    "节点执行失败: nodeNameLength={}, errorType={}, errorMessageLength={}",
                    valueLength(config.getNodeName()),
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            if (executionHistoryService != null && executionId != null) {
                executionHistoryService.completeExecution(
                        actor, executionId, null, 2, e.getMessage());
            }
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 1000) {
                log.info(
                        "节点执行耗时: nodeNamePresent={}, durationMs={}",
                        config.getNodeName() != null,
                        duration);
            }
        }
    }

    private NodeOutput executeWithTimeout(NodeInput input, long timeoutMs) throws Exception {
        if (timeoutMs <= 0) {
            return doExecute(input);
        }
        ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();
        Future<NodeOutput> future = executor.submit(() -> doExecute(input));
        try {
            return future.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new Exception("节点执行超时 (" + timeoutMs + "ms): " + config.getNodeName());
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * {@code beforeExecute} 执行当前类型定义的业务操作。
     *
     * @param stateData 参数值，用于执行当前操作。
     */
    protected void beforeExecute(Map<String, Object> stateData) {
        log.info("开始执行节点: nodeNamePresent={}", config.getNodeName() != null);
        if ("DEBUG".equalsIgnoreCase(config.getLogLevel())) {
            log.debug(
                    "节点配置: nodeIdPresent={}, nodeTypePresent={}, timeoutMs={}, retryCount={},"
                            + " errorStrategyPresent={}",
                    config.getNodeId() != null,
                    config.getNodeType() != null,
                    config.getTimeout(),
                    config.getRetryCount(),
                    config.getErrorStrategy() != null);
        }
    }

    /**
     * {@code processParameters} 执行当前模块定义的业务流程。
     *
     * @param stateData 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected NodeInput processParameters(Map<String, Object> stateData) {
        log.debug("处理节点参数: nodeNamePresent={}", config.getNodeName() != null);
        return NodeInput.fromMap(stateData);
    }

    /**
     * {@code doExecute} 执行当前类型定义的业务操作。
     *
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected abstract NodeOutput doExecute(NodeInput input) throws Exception;

    /**
     * {@code afterExecute} 执行当前类型定义的业务操作。
     *
     * @param stateData 参数值，用于执行当前操作。
     * @param output 参数值，用于执行当前操作。
     */
    protected void afterExecute(Map<String, Object> stateData, NodeOutput output) {
        log.info("节点执行完成: nodeNamePresent={}", config.getNodeName() != null);
    }

    /**
     * {@code handleException} 执行当前模块定义的业务流程。
     *
     * @param stateData 参数值，用于执行当前操作。
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected Map<String, Object> handleException(Map<String, Object> stateData, Exception e) {
        String errorStrategy = config.getErrorStrategy();

        switch (errorStrategy) {
            case "IGNORE":
                log.warn(
                        "忽略节点异常，继续执行: errorType={}, errorMessageLength={}",
                        e.getClass().getSimpleName(),
                        valueLength(e.getMessage()));
                return Collections.emptyMap();

            case "DEFAULT":
                log.warn(
                        "使用默认值处理节点异常: errorType={}, errorMessageLength={}",
                        e.getClass().getSimpleName(),
                        valueLength(e.getMessage()));
                return createDefaultResult();

            case "THROW":
            default:
                log.error(
                        "抛出节点异常: errorType={}, errorMessageLength={}",
                        e.getClass().getSimpleName(),
                        valueLength(e.getMessage()));
                throw new RuntimeException("节点执行失败: " + config.getNodeName(), e);
        }
    }

    /**
     * {@code createDefaultResult} 写入或更新当前模块中的业务数据。
     *
     * @return 返回当前操作产生的结果。
     */
    protected Map<String, Object> createDefaultResult() {
        return Map.of(NodeFields.FieldKey.ERROR.key(), "使用默认值处理", "status", "DEFAULT_APPLIED");
    }

    private static int valueLength(String value) {
        return value == null ? 0 : value.length();
    }

    /**
     * 获取该节点所需的输入参数定义列表
     *
     * <p>子类应覆盖此方法，返回该节点从 AgentState 读取的所有入参的元信息。 这些信息将被 AgentLoader 聚合后存入 agent_version / agent_def
     * 的 ext_fields， 用于接口文档推导和运行时参数校验。
     *
     * @return 输入参数定义列表，子类覆盖时不能返回 null（返回空列表即可）
     */
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.Collections.emptyList();
    }

    private static String getStr(Map<String, Object> data, NodeFields.FieldKey key) {
        Object v = data.get(key.key());
        return v != null ? v.toString() : null;
    }

    private static Long getLong(Map<String, Object> data, NodeFields.FieldKey key) {
        Object v = data.get(key.key());
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static ActorContext actor(Map<String, Object> data) {
        Long tenantId = getLong(data, "tenantId");
        Long userId = getLong(data, NodeFields.FieldKey.USER_ID);
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalStateException("tenant context is required");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalStateException("user context is required");
        }
        return new ActorContext(new TenantId(tenantId), new UserId(userId), false);
    }

    private static Long getLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number number) return number.longValue();
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
