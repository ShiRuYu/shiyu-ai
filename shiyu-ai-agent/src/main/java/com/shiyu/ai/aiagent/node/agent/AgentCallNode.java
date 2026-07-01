package com.shiyu.ai.aiagent.node.agent;

import com.shiyu.ai.aiagent.service.AgentService;
import com.shiyu.ai.aiagent.node.BaseNode;
import com.shiyu.ai.aiagent.node.NodeInput;
import com.shiyu.ai.aiagent.node.NodeOutput;
import com.shiyu.ai.aiagent.node.NodeType;
import com.shiyu.ai.aiagent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import com.shiyu.ai.aiagent.node.NodeInputParam;

/**
 * Agent 调用节点
 * 用于在 Graph 中调用其他已注册的 Agent 执行子任务
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
     * Agent 服务（必须依赖）
     */
    private final AgentService agentService;

    /**
     * 私有构造函数，强制使用 Builder 模式
     *
     * @param config       节点配置
     * @param agentService Agent 服务
     */
    private AgentCallNode(AgentCallConfig config, AgentService agentService) {
        super(config != null ? config : new AgentCallConfig());
        this.config = config != null ? config : new AgentCallConfig();
        this.config.setNodeType(NodeType.AGENT_CALL);
        this.agentService = agentService;
    }

    /**
     * 获取 Builder 实例
     *
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 AgentCallNode 实例
     */
    public static class Builder {
        private AgentCallConfig config;
        private AgentService agentService;

        /**
         * 设置节点配置
         *
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(AgentCallConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置 Agent 服务
         *
         * @param agentService Agent 服务
         * @return Builder 实例
         */
        public Builder agentService(AgentService agentService) {
            this.agentService = agentService;
            return this;
        }

        /**
         * 构建并返回 AgentCallNode 实例
         *
         * @return AgentCallNode 实例
         * @throws IllegalStateException 如果校验失败
         */
        public AgentCallNode build() {
            if (agentService == null) {
                throw new IllegalStateException("创建 AgentCallNode 失败：agentService 不能为空");
            }
            return new AgentCallNode(config, agentService);
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

            // 4. 调用目标 Agent
            log.info("开始调用目标 Agent：agentId={}, version={}", targetAgentId, targetVersion);
            Map<String, Object> result;
            if (targetVersion != null && !targetVersion.trim().isEmpty()) {
                result = agentService.execute(targetAgentId, targetVersion, agentInput);
            } else {
                result = agentService.execute(targetAgentId, agentInput);
            }

            // 5. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("Agent 调用成功");

            String outputKey = config.getOutputKey() != null ? config.getOutputKey() : "agentResult";
            output.addData(outputKey, result);
            output.addData(FieldKey.TARGET_AGENT_ID, targetAgentId);
            output.addData(FieldKey.TARGET_VERSION, targetVersion);

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
        // 优先使用输入中的配置
        String targetAgentId = input.getParameter(FieldKey.TARGET_AGENT_ID);
        if (targetAgentId != null && !targetAgentId.trim().isEmpty()) {
            return targetAgentId;
        }
        return config.getTargetAgentId();
    }

    /**
     * 获取目标版本
     */
    private String getTargetVersion(NodeInput input) {
        // 优先使用输入中的配置
        String targetVersion = input.getParameter(FieldKey.TARGET_VERSION);
        if (targetVersion != null && !targetVersion.trim().isEmpty()) {
            return targetVersion;
        }
        return config.getTargetVersion();
    }

    /**
     * 准备目标 Agent 的输入参数
     * 根据 inputMapping 配置进行参数映射
     */
    private Map<String, Object> prepareAgentInput(NodeInput input) {
        Map<String, Object> agentInput = new HashMap<>();
        Map<String, String> inputMapping = config.getInputMapping();

        if (inputMapping != null && !inputMapping.isEmpty()) {
            // 按照映射规则转换参数
            for (Map.Entry<String, String> entry : inputMapping.entrySet()) {
                String sourceKey = entry.getKey();
                String targetKey = entry.getValue();
                Object value = input.getParameter(sourceKey);
                if (value != null) {
                    agentInput.put(targetKey, value);
                }
            }
        } else {
            // 没有映射规则时，传递所有参数（排除内部字段）
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
