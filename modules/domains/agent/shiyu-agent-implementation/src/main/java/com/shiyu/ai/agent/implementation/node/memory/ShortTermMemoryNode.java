package com.shiyu.ai.agent.implementation.node.memory;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.implementation.runtime.model.AgentExecutionContext;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code ShortTermMemoryNode} 承载智能体模块中的智能流程节点，负责执行本节点的输入处理与结果产出。
 */
@Setter
@Getter
@Slf4j
public class ShortTermMemoryNode extends BaseNode {

    /**
     * 配置，表示当前对象中的对应属性。
     */
    private ShortTermMemoryConfig config;

    /**
     * memoryService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentExecutionContext memoryService;

    private ShortTermMemoryNode(ShortTermMemoryConfig config, AgentExecutionContext memoryService) {
        super(config != null ? config : new ShortTermMemoryConfig());
        this.config = config != null ? config : new ShortTermMemoryConfig();
        this.config.setNodeType(NodeType.MEMORY_SHORT_TERM);
        this.memoryService = memoryService;
    }

    /**
     * {@code builder} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@code Builder} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    public static class Builder {
        /**
         * 配置，表示当前对象中的对应属性。
         */
        private ShortTermMemoryConfig config;
        /**
         * memoryService 属性，保存当前对象中的业务数据或协作依赖。
         */
        private AgentExecutionContext memoryService;

        /**
         * {@code config} 执行当前类型定义的业务操作。
         *
         * @param config 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder config(ShortTermMemoryConfig config) {
            this.config = config;
            return this;
        }

        /**
         * {@code memoryService} 执行当前类型定义的业务操作。
         *
         * @param memoryService 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder memoryService(AgentExecutionContext memoryService) {
            this.memoryService = memoryService;
            return this;
        }

        /**
         * {@code build} 执行当前类型定义的业务操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public ShortTermMemoryNode build() {
            if (memoryService == null) {
                throw new IllegalStateException("创建 ShortTermMemoryNode 失败: memoryService 不能为空");
            }
            return new ShortTermMemoryNode(config, memoryService);
        }
    }

    /**
     * {@code doExecute} 执行当前类型定义的业务操作。
     *
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行短期记忆节点: {}", config.getNodeName());

        try {
            String sessionId = input.getParameter(FieldKey.SESSION_ID, "");
            Long tenantId = input.getParameter(FieldKey.TENANT_ID, null);
            Long userId = input.getParameter(FieldKey.USER_ID, null);
            String agentId = input.getParameter(FieldKey.AGENT_ID, "");
            String userMessage = input.getParameter(FieldKey.QUERY, "");
            String assistantResponse =
                    input.getParameter(FieldKey.CONTENT, input.getParameter(FieldKey.RESPONSE, ""));
            int maxMessages = config.getMaxMessages() != null ? config.getMaxMessages() : 20;
            boolean slidingWindow =
                    config.getEnableSlidingWindow() != null && config.getEnableSlidingWindow();

            if (sessionId.isEmpty()) {
                log.warn("sessionId 为空，跳过短期记忆存储");
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("短期记忆节点执行跳过: sessionId 为空");
                return output;
            }

            if (userId == null || userId <= 0) {
                log.warn("userId 为空或非法，拒绝写入短期记忆");
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("短期记忆节点执行跳过: userId 为空或非法");
                return output;
            }

            if (tenantId == null || tenantId <= 0) {
                log.warn("tenantId 为空或非法，拒绝写入短期记忆");
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("短期记忆节点执行跳过: tenantId 为空或非法");
                return output;
            }
            TenantId tenant = new TenantId(tenantId);

            if (!userMessage.isEmpty()) {
                memoryService.append(tenant, sessionId, "user", userMessage);
            }
            if (!assistantResponse.isEmpty()) {
                memoryService.append(tenant, sessionId, "assistant", assistantResponse);
            }

            String conversationHistory =
                    String.join("\n", memoryService.messages(tenant, sessionId, maxMessages));

            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("短期记忆节点执行成功");
            output.addData(FieldKey.CONVERSATION_HISTORY, conversationHistory);
            output.addData(FieldKey.MESSAGES, conversationHistory);

            log.info("短期记忆节点执行完成, 会话历史长度: {} 字符", conversationHistory.length());
            return output;

        } catch (Exception e) {
            log.error("短期记忆节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("短期记忆节点执行失败，请稍后重试");
            return output;
        }
    }

    /**
     * {@code getRequiredInputs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.previous("sessionId", "string", "会话 ID"),
                NodeInputParam.previous("agentId", "string", "Agent ID"),
                NodeInputParam.previous("tenantId", "number", "租户 ID"),
                NodeInputParam.previous("userId", "number", "用户 ID"),
                NodeInputParam.previous("query", "string", "用户输入"),
                NodeInputParam.previous("content", "string", "AI 回复内容"));
    }
}
