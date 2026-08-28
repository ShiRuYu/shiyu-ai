package com.shiyu.ai.agent.node.memory;

import com.shiyu.ai.memory.magma.ConfirmationPolicy;
import com.shiyu.ai.memory.magma.IngestMemoryCommand;
import com.shiyu.ai.memory.magma.MemoryIngestionPort;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import com.shiyu.ai.agent.node.NodeInputParam;

@Setter
@Getter
@Slf4j
public class LongTermMemoryNode extends BaseNode {

    private LongTermMemoryConfig config;

    private final MemoryIngestionPort memoryService;

    private LongTermMemoryNode(LongTermMemoryConfig config, MemoryIngestionPort memoryService) {
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
        private MemoryIngestionPort memoryService;

        public Builder config(LongTermMemoryConfig config) {
            this.config = config;
            return this;
        }

        public Builder memoryService(MemoryIngestionPort memoryService) {
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
            Long tenantId = input.getParameter(FieldKey.TENANT_ID, null);
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

            if (tenantId == null || userId == null || userId <= 0) throw new IllegalArgumentException("tenantId and userId are required");
            memoryService.ingest(new IngestMemoryCommand(new TenantId(tenantId), "agent", "USER", String.valueOf(userId), category, memoryContent, java.time.Instant.now(), "AGENT_EXECUTION", sessionId, Map.of("agentId", agentId, "memoryKey", memoryKey), 0.8, importance, ConfirmationPolicy.REQUIRED));

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
            NodeInputParam.previous("tenantId", "number", "租户 ID"),
            NodeInputParam.previous("userId", "number", "用户 ID"),
            NodeInputParam.previous("sessionId", "string", "会话 ID"),
            NodeInputParam.config("memoryKey", "string", "记忆键"),
            NodeInputParam.config("category", "string", "记忆分类"),
            NodeInputParam.config("importance", "number", "重要度（0-1）")
        );
    }
}
