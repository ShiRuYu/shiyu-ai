package com.shiyu.ai.aiagent.node.memory;

import com.shiyu.ai.memory.MemoryService;
import com.shiyu.ai.aiagent.node.BaseNode;
import com.shiyu.ai.aiagent.node.NodeInput;
import com.shiyu.ai.aiagent.node.NodeOutput;
import com.shiyu.ai.aiagent.node.NodeType;
import com.shiyu.ai.aiagent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import com.shiyu.ai.aiagent.node.NodeInputParam;

@Setter
@Getter
@Slf4j
public class LongTermMemoryNode extends BaseNode {

    private LongTermMemoryConfig config;

    private final MemoryService memoryService;

    private LongTermMemoryNode(LongTermMemoryConfig config, MemoryService memoryService) {
        super(config != null ? config : new LongTermMemoryConfig());
        this.config = config != null ? config : new LongTermMemoryConfig();
        this.config.setNodeType(NodeType.MEMORY_LONG_TERM);
        this.memoryService = memoryService;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LongTermMemoryConfig config;
        private MemoryService memoryService;

        public Builder config(LongTermMemoryConfig config) {
            this.config = config;
            return this;
        }

        public Builder memoryService(MemoryService memoryService) {
            this.memoryService = memoryService;
            return this;
        }

        public LongTermMemoryNode build() {
            if (memoryService == null) {
                throw new IllegalStateException("创建 LongTermMemoryNode 失败: memoryService 不能为空");
            }
            return new LongTermMemoryNode(config, memoryService);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行长期记忆节点: {}", config.getNodeName());

        try {
            Long userId = input.getParameter(FieldKey.USER_ID, null);
            String agentId = input.getParameter(FieldKey.AGENT_ID, "");
            String sessionId = input.getParameter(FieldKey.SESSION_ID, "");

            String memoryKey = input.getParameter(FieldKey.MEMORY_KEY, "");
            String memoryContent = input.getParameter(FieldKey.MEMORY_CONTENT, "");
            String category = input.getParameter(FieldKey.CATEGORY, "general");
            double minImportance = config.getMinImportanceScore() != null ? config.getMinImportanceScore() : 0.5;
            double importance = input.getParameter(FieldKey.IMPORTANCE, 0.5);

            if (memoryContent == null || memoryContent.isBlank()) {
                log.warn("长期记忆内容为空，跳过存储");
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.setMsg("长期记忆节点跳过: 内容为空");
                return output;
            }

            if (importance < minImportance) {
                log.info("记忆重要度 {} 低于阈值 {}, 跳过存储", importance, minImportance);
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.setMsg("长期记忆节点跳过: 重要度不足");
                return output;
            }

            memoryService.saveLongTermMemory(userId, agentId, category, memoryKey, memoryContent, importance, sessionId);

            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("长期记忆节点执行成功");
            output.addData(FieldKey.MEMORY_KEY, memoryKey);
            output.addData(FieldKey.IMPORTANCE, importance);

            log.info("长期记忆已保存: category={}, importance={}", category, importance);
            return output;

        } catch (Exception e) {
            log.error("长期记忆节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("长期记忆节点执行失败: " + e.getMessage());
            return output;
        }
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.previous("userId", "number", "用户 ID"),
            NodeInputParam.previous("sessionId", "string", "会话 ID"),
            NodeInputParam.config("memoryKey", "string", "记忆键"),
            NodeInputParam.config("category", "string", "记忆分类"),
            NodeInputParam.config("importance", "number", "重要度（0-1）")
        );
    }
}
