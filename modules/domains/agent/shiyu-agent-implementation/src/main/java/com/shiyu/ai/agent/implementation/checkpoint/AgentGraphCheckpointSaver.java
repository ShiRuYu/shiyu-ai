package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.agent.contract.node.NodeActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 将 LangGraph4j 的节点检查点持久化到租户范围内的 Agent 检查点仓储。
 * RunnableConfig 的 threadId 必须是已经授权的执行标识。
 */
public final class AgentGraphCheckpointSaver implements BaseCheckpointSaver {
    public static final String APPROVAL_ID_KEY = "agentApprovalId";
    private static final String STARTED_NODE = "__agent_node_started__";
    private static final String NONRESUMABLE_NODE = "__agent_nonresumable__";
    private static final String APPROVAL_PENDING_NODE = "__agent_approval_pending__";
    private final TenantId tenantId;
    private final String executionId;
    private final CheckpointManager manager;
    private final Consumer<String> checkpointRecorded;

    /**
     * 创建只服务于一次已授权执行的检查点适配器。
     *
     * @param tenantId 执行所属租户
     * @param executionId 执行标识
     * @param manager 检查点管理器
     */
    public AgentGraphCheckpointSaver(TenantId tenantId, String executionId,
            CheckpointManager manager) {
        this(tenantId, executionId, manager, ignored -> {});
    }

    /** Record the checkpoint ID on its execution only after its data is durable. */
    public AgentGraphCheckpointSaver(TenantId tenantId, String executionId,
            CheckpointManager manager, Consumer<String> checkpointRecorded) {
        if (tenantId == null || executionId == null || executionId.isBlank() || manager == null) {
            throw new IllegalArgumentException("图检查点需要租户、执行标识和仓储");
        }
        if (checkpointRecorded == null) {
            throw new IllegalArgumentException("图检查点需要执行记录回调");
        }
        this.tenantId = tenantId;
        this.executionId = executionId;
        this.manager = manager;
        this.checkpointRecorded = checkpointRecorded;
    }

    /**
     * 列出当前执行可安全恢复的图检查点，隐藏未完成节点标记。
     *
     * @param config 包含已授权执行标识的运行配置
     * @return 本次执行可安全恢复的图检查点
     */
    @Override
    public Collection<org.bsc.langgraph4j.checkpoint.Checkpoint> list(RunnableConfig config) {
        requireExecution(config);
        return manager.listCheckpoints(tenantId, executionId).stream()
                .filter(checkpoint -> checkpoint.getNextNodeId() != null
                        && !requiresManualRecovery(checkpoint))
                .map(AgentGraphCheckpointSaver::toGraphCheckpoint)
                .toList();
    }

    /**
     * 读取最新图检查点，并拒绝旧检查点回放及未确认的节点执行。
     *
     * @param config 包含已授权执行标识及可选检查点标识的运行配置
     * @return 存在时返回最新可恢复图检查点
     */
    @Override
    public Optional<org.bsc.langgraph4j.checkpoint.Checkpoint> get(RunnableConfig config) {
        requireExecution(config);
        Checkpoint checkpoint = manager.loadLatestCheckpoint(tenantId, executionId);
        if (checkpoint == null || !executionId.equals(checkpoint.getExecutionId())
                || !tenantId.equals(checkpoint.getTenantId())
                || checkpoint.getNextNodeId() == null) {
            return Optional.empty();
        }
        if (isStartedMarker(checkpoint)) {
            throw new IllegalStateException("节点可能已执行但结果检查点未保存，拒绝重复副作用");
        }
        if (isNonresumableMarker(checkpoint)) {
            throw new IllegalStateException("图状态含无法无损恢复的值，拒绝自动续跑");
        }
        if (config.checkPointId().isPresent()
                && !config.checkPointId().get().equals(checkpoint.getCheckpointId())) {
            throw new IllegalStateException("指定检查点不是最新检查点，拒绝回放旧节点");
        }
        return Optional.of(toGraphCheckpoint(checkpoint));
    }

    /**
     * 在调用节点前持久化执行意图；若节点执行后未写成结果检查点，恢复必须拒绝重放。
     *
     * @param config 当前图运行配置
     * @param nodeId 即将执行的节点
     * @param state 节点执行前的图状态
     */
    public void markNodeStarted(RunnableConfig config, String nodeId,
            Map<String, Object> state) {
        requireExecution(config);
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("节点标识不能为空");
        }
        saveAndRecord(new Checkpoint(tenantId, executionId,
                UUID.randomUUID().toString().replace("-", ""),
                STARTED_NODE, nodeId, Map.of()));
    }

    /** Save the state before a tool that has requested approval without executing it. */
    public void markApprovalPending(RunnableConfig config, String nodeId,
            Map<String, Object> state, String approvalId) {
        requireExecution(config);
        if (nodeId == null || nodeId.isBlank()
                || approvalId == null || approvalId.isBlank()) {
            throw new IllegalArgumentException("approval checkpoint requires node and approval ID");
        }
        Map<String, Object> pendingState = new LinkedHashMap<>(NodeActorContext.publicData(state));
        pendingState.put(APPROVAL_ID_KEY, approvalId);
        CheckpointStateCodec.typeHints(pendingState);
        saveAndRecord(new Checkpoint(tenantId, executionId,
                UUID.randomUUID().toString().replace("-", ""),
                APPROVAL_PENDING_NODE, nodeId, pendingState));
    }

    /**
     * 保存节点结果；不能无损恢复的状态改写为禁止自动续跑标记。
     *
     * @param config 当前图运行配置
     * @param checkpoint LangGraph4j 生成的节点结果检查点
     * @return 带持久化检查点标识的运行配置
     */
    @Override
    public RunnableConfig put(RunnableConfig config,
            org.bsc.langgraph4j.checkpoint.Checkpoint checkpoint) {
        requireExecution(config);
        if (checkpoint == null || checkpoint.getId() == null
                || checkpoint.getNextNodeId() == null) {
            throw new IllegalArgumentException("图检查点缺少标识或下一节点");
        }
        Map<String, Object> publicState = NodeActorContext.publicData(checkpoint.getState());
        boolean resumable;
        try {
            CheckpointStateCodec.typeHints(publicState);
            resumable = true;
        } catch (IllegalArgumentException unsupportedState) {
            resumable = false;
        }
        saveAndRecord(new Checkpoint(tenantId, executionId, checkpoint.getId(),
                resumable ? checkpoint.getNodeId() : NONRESUMABLE_NODE,
                checkpoint.getNextNodeId(), resumable ? publicState : Map.of()));
        return config.withCheckPointId(checkpoint.getId());
    }

    private void saveAndRecord(Checkpoint checkpoint) {
        manager.saveCheckpoint(checkpoint);
        checkpointRecorded.accept(checkpoint.getCheckpointId());
    }

    /**
     * 生成已持久化检查点的释放标记，实际清理由执行生命周期控制。
     *
     * @param config 当前图运行配置
     * @return 本次执行检查点的释放标记
     */
    @Override
    public Tag release(RunnableConfig config) {
        requireExecution(config);
        return new Tag(executionId, List.copyOf(list(config)));
    }

    private void requireExecution(RunnableConfig config) {
        if (config == null || !executionId.equals(config.threadId().orElse(null))) {
            throw new IllegalStateException("图检查点执行标识不匹配");
        }
    }

    /**
     * 判断最新检查点是否表示节点已开始但结果尚未持久化。
     *
     * @param checkpoint 待检查的检查点
     * @return 需要人工处理、禁止自动重放时返回 true
     */
    public static boolean isStartedMarker(Checkpoint checkpoint) {
        if (checkpoint == null) return false;
        return STARTED_NODE.equals(checkpoint.getNodeId());
    }

    /** Whether the node is safely waiting before its approved side effect. */
    public static boolean isApprovalPending(Checkpoint checkpoint) {
        return checkpoint != null && APPROVAL_PENDING_NODE.equals(checkpoint.getNodeId());
    }

    /**
     * 判断检查点是否缺少安全自动恢复所需的节点结果或状态类型。
     *
     * @param checkpoint 待检查的检查点
     * @return 需要人工处理时返回 true
     */
    public static boolean requiresManualRecovery(Checkpoint checkpoint) {
        return isStartedMarker(checkpoint) || isNonresumableMarker(checkpoint);
    }

    private static boolean isNonresumableMarker(Checkpoint checkpoint) {
        return checkpoint != null && NONRESUMABLE_NODE.equals(checkpoint.getNodeId());
    }

    private static org.bsc.langgraph4j.checkpoint.Checkpoint toGraphCheckpoint(
            Checkpoint checkpoint) {
        return org.bsc.langgraph4j.checkpoint.Checkpoint.builder()
                .id(checkpoint.getCheckpointId())
                .nodeId(checkpoint.getNodeId())
                .nextNodeId(checkpoint.getNextNodeId())
                .state(checkpoint.getState())
                .build();
    }
}
