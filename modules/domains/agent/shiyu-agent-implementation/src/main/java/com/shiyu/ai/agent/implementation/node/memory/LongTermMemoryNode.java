package com.shiyu.ai.agent.implementation.node.memory;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.api.MemoryIngestionPort;
import com.shiyu.ai.memory.contract.model.ConfirmationPolicy;
import com.shiyu.ai.memory.contract.model.IngestMemoryCommand;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 执行 Long Term 记忆 相关流程节点的输入处理和状态转移。
 */
@Setter
@Getter
@Slf4j
public class LongTermMemoryNode extends BaseNode {

    /**
     * 配置，表示当前对象中的对应属性。
     */
    private LongTermMemoryConfig config;

    /**
     * memoryService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final MemoryIngestionPort memoryService;

    private LongTermMemoryNode(LongTermMemoryConfig config, MemoryIngestionPort memoryService) {
        super(config != null ? config : new LongTermMemoryConfig());
        this.config = config != null ? config : new LongTermMemoryConfig();
        this.config.setNodeType(NodeType.MEMORY_LONG_TERM);
        this.memoryService = memoryService;
    }

    /**
     * 构建或转换 Long Term 记忆 相关业务数据，并返回处理结果。
     *
     * @return 返回 Long Term 记忆 相关操作生成的结果数据。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建 Builder 相关的对象、流程或运行时配置。
     */
    public static class Builder {
        /**
         * 配置，表示当前对象中的对应属性。
         */
        private LongTermMemoryConfig config;
        /**
         * memoryService 属性，保存当前对象中的业务数据或协作依赖。
         */
        private MemoryIngestionPort memoryService;

        /**
         * 执行 Builder 相关业务数据，并返回处理结果。
         *
         * @param config 用于完成本次业务处理的 config 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder config(LongTermMemoryConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 执行 Builder 相关业务数据，并返回处理结果。
         *
         * @param memoryService 用于完成本次业务处理的 memoryService 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder memoryService(MemoryIngestionPort memoryService) {
            this.memoryService = memoryService;
            return this;
        }

        /**
         * 构建或转换 Builder 相关业务数据，并返回处理结果。
         *
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public LongTermMemoryNode build() {
            if (memoryService == null) {
                throw new IllegalStateException("创建 LongTermMemoryNode 失败: memoryService 不能为空");
            }
            return new LongTermMemoryNode(config, memoryService);
        }
    }

    /**
     * 执行 Long Term 记忆 相关业务数据，并返回处理结果。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 Long Term 记忆 相关操作生成的结果数据。
     */
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
            double minImportance =
                    config.getMinImportanceScore() != null ? config.getMinImportanceScore() : 0.5;
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

            if (tenantId == null || userId == null || userId <= 0)
                throw new IllegalArgumentException("tenantId and userId are required");
            memoryService.ingest(
                    new IngestMemoryCommand(
                            new TenantId(tenantId),
                            "agent",
                            "USER",
                            String.valueOf(userId),
                            category,
                            memoryContent,
                            java.time.Instant.now(),
                            "AGENT_EXECUTION",
                            sessionId,
                            Map.of("agentId", agentId, "memoryKey", memoryKey),
                            0.8,
                            importance,
                            ConfirmationPolicy.REQUIRED));

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
            output.setMsg("长期记忆节点执行失败，请稍后重试");
            return output;
        }
    }

    /**
     * 查询 Long Term 记忆 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.previous("tenantId", "number", "租户 ID"),
                NodeInputParam.previous("userId", "number", "用户 ID"),
                NodeInputParam.previous("sessionId", "string", "会话 ID"),
                NodeInputParam.config("memoryKey", "string", "记忆键"),
                NodeInputParam.config("category", "string", "记忆分类"),
                NodeInputParam.config("importance", "number", "重要度（0-1）"));
    }
}
