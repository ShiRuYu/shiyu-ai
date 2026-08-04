package com.shiyu.ai.agent.node.agent;

import com.shiyu.ai.agent.runtime.AgentRuntime;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import com.shiyu.ai.agent.node.NodeInputParam;

/**
 * Agent 调用节点
 * 用于在 Graph 中调用其他已注册的 Agent 执行子任务
 * 使用 AgentRuntime 进行调用（统一走 Execution 生命周期）
 *
 * @author shiyu-ai
 * @date 2026-06-06
 */
@Setter
@Getter
@Slf4j
public class AgentCallNode extends BaseNode {

    private AgentCallConfig config;

    /**
     * Agent 运行时（必须依赖）
     */
    private final AgentRuntime agentRuntime;

    /**
     * 私有构造函数，强制使用 Builder 模式
     */
    private AgentCallNode(AgentCallConfig config, AgentRuntime agentRuntime) {
        super(config != null ? config : new AgentCallConfig());
        this.config = config != null ? config : new AgentCallConfig();
        this.config.setNodeType(NodeType.AGENT_CALL);
        this.agentRuntime = agentRuntime;
    }

    /**
     * 获取 Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 AgentCallNode 实例
     */
    public static class Builder {
        private AgentCallConfig config;
        private AgentRuntime agentRuntime;

        public Builder config(AgentCallConfig config) {
            this.config = config;
            return this;
        }

        public Builder agentRuntime(AgentRuntime agentRuntime) {
            this.agentRuntime = agentRuntime;
            return this;
        }

        public AgentCallNode build() {
            if (agentRuntime == null) {
                throw new IllegalStateException("创建 AgentCallNode 失败：agentRuntime 不能为空");
            }
            return new AgentCallNode(config, agentRuntime);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 Agent 调用节点：{}", config.getNodeName());
        log.debug("Agent 调用配置：targetAgentId={}, targetVersion={}, outputKey={}",
                config.getTargetAgentId(), config.getTargetVersion(), config.getOutputKey());

        try {
            // 1. 获取目标 Agent ID
            String targetAgentId = getTargetAgentId(input);
            if (targetAgentId == null || targetAgentId.trim().isEmpty()) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("目标 Agent ID 不能为空");
                return output;
            }

            // 2. 准备输入参数
            Map<String, Object> agentInput = prepareAgentInput(input);

            // 3. 获取目标版本
            String targetVersion = getTargetVersion(input);

            // 4. 调用目标 Agent（统一走 AgentRuntime，获得 Execution 生命周期支持）
            log.info("开始调用目标 Agent：agentId={}, version={}", targetAgentId, targetVersion);
            var execution = (targetVersion != null && !targetVersion.trim().isEmpty())
                    ? agentRuntime.execute(targetAgentId, targetVersion, agentInput)
                    : agentRuntime.execute(targetAgentId, agentInput);

            Map<String, Object> result = execution.getOutput();

            // 5. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("Agent 调用成功");

            String outputKey = config.getOutputKey() != null ? config.getOutputKey() : "agentResult";
            output.addData(outputKey, result);
            output.addData(FieldKey.TARGET_AGENT_ID.key(), targetAgentId);
            output.addData(FieldKey.TARGET_VERSION.key(), targetVersion);

            log.info("Agent 调用成功：targetAgentId={}, resultSize={}",
                    targetAgentId, result != null ? result.size() : 0);
            return output;

        } catch (Exception e) {
            log.error("Agent 调用节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("Agent 调用节点执行失败：" + e.getMessage());
            return output;
        }
    }

    /**
     * 获取目标 Agent ID
     */
    private String getTargetAgentId(NodeInput input) {
        String targetAgentId = input.getParameter(FieldKey.TARGET_AGENT_ID.key());
        if (targetAgentId != null && !targetAgentId.trim().isEmpty()) {
            return targetAgentId;
        }
        return config.getTargetAgentId();
    }

    /**
     * 获取目标版本
     */
    private String getTargetVersion(NodeInput input) {
        String targetVersion = input.getParameter(FieldKey.TARGET_VERSION.key());
        if (targetVersion != null && !targetVersion.trim().isEmpty()) {
            return targetVersion;
        }
        return config.getTargetVersion();
    }

    /**
     * 准备目标 Agent 的输入参数
     */
    private Map<String, Object> prepareAgentInput(NodeInput input) {
        Map<String, Object> agentInput = new HashMap<>();
        Map<String, String> inputMapping = config.getInputMapping();

        if (inputMapping != null && !inputMapping.isEmpty()) {
            for (Map.Entry<String, String> entry : inputMapping.entrySet()) {
                String sourceKey = entry.getKey();
                String targetKey = entry.getValue();
                Object value = input.getParameter(sourceKey);
                if (value != null) {
                    agentInput.put(targetKey, value);
                }
            }
        } else {
            for (Map.Entry<String, Object> entry : input.toMap().entrySet()) {
                String key = entry.getKey();
                if (!FieldKey.TARGET_AGENT_ID.key().equals(key) && !FieldKey.TARGET_VERSION.key().equals(key)) {
                    agentInput.put(key, entry.getValue());
                }
            }
        }

        return agentInput;
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.config("targetAgentId", "string", "目标 Agent ID"),
            NodeInputParam.config("targetVersion", "string", "目标版本号"),
            NodeInputParam.previous("query", "string", "用户输入（传递给子 Agent）")
        );
    }
}
