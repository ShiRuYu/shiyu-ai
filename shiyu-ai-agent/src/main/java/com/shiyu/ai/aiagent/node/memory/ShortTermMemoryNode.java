package com.shiyu.ai.aiagent.node.memory;

import com.shiyu.ai.core.memory.MemoryService;
import com.shiyu.ai.aiagent.node.BaseNode;
import com.shiyu.ai.aiagent.node.NodeInput;
import com.shiyu.ai.aiagent.node.NodeOutput;
import com.shiyu.ai.aiagent.node.NodeType;
import com.shiyu.ai.aiagent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import com.shiyu.ai.aiagent.node.NodeInputParam;

@Setter
@Getter
@Slf4j
public class ShortTermMemoryNode extends BaseNode {

    private ShortTermMemoryConfig config;

    private final MemoryService memoryService;

    private ShortTermMemoryNode(ShortTermMemoryConfig config, MemoryService memoryService) {
        super(config != null ? config : new ShortTermMemoryConfig());
        this.config = config != null ? config : new ShortTermMemoryConfig();
        this.config.setNodeType(NodeType.MEMORY_SHORT_TERM);
        this.memoryService = memoryService;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ShortTermMemoryConfig config;
        private MemoryService memoryService;

        public Builder config(ShortTermMemoryConfig config) {
            this.config = config;
            return this;
        }

        public Builder memoryService(MemoryService memoryService) {
            this.memoryService = memoryService;
            return this;
        }

        public ShortTermMemoryNode build() {
            if (memoryService == null) {
                throw new IllegalStateException("创建 ShortTermMemoryNode 失败: memoryService 不能为空");
            }
            return new ShortTermMemoryNode(config, memoryService);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行短期记忆节点: {}", config.getNodeName());

        try {
            String sessionId = input.getParameter(FieldKey.SESSION_ID, "");
            Long userId = input.getParameter(FieldKey.USER_ID, null);
            String agentId = input.getParameter(FieldKey.AGENT_ID, "");
            String userMessage = input.getParameter(FieldKey.QUERY, "");
            String assistantResponse = input.getParameter(FieldKey.CONTENT, input.getParameter(FieldKey.RESPONSE, ""));
            int maxMessages = config.getMaxMessages() != null ? config.getMaxMessages() : 20;
            boolean slidingWindow = config.getEnableSlidingWindow() != null && config.getEnableSlidingWindow();

            if (sessionId.isEmpty()) {
                log.warn("sessionId 为空，跳过短期记忆存储");
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("短期记忆节点执行跳过: sessionId 为空");
                return output;
            }

            if (!userMessage.isEmpty()) {
                memoryService.saveMessage(sessionId, userId, agentId, "user", userMessage);
            }
            if (!assistantResponse.isEmpty()) {
                memoryService.saveMessage(sessionId, userId, agentId, "assistant", assistantResponse);
            }

            String conversationHistory = memoryService.buildConversationHistory(sessionId, maxMessages);

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
            output.setMsg("短期记忆节点执行失败: " + e.getMessage());
            return output;
        }
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.previous("sessionId", "string", "会话 ID"),
            NodeInputParam.previous("agentId", "string", "Agent ID"),
            NodeInputParam.previous("query", "string", "用户输入"),
            NodeInputParam.previous("content", "string", "AI 回复内容")
        );
    }
}
