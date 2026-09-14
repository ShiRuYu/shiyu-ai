package com.shiyu.ai.agent.implementation.node.memory;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.implementation.node.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.api.MemoryQueryPort;
import com.shiyu.ai.memory.contract.model.*;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * {@code MemoryRetrievalNode} 承载智能体模块中的智能流程节点，负责执行本节点的输入处理与结果产出。
 */
@Setter
@Getter
@Slf4j
public class MemoryRetrievalNode extends BaseNode {
    /**
     * 配置，表示当前对象中的对应属性。
     */
    private MemoryRetrievalConfig config;
    /**
     * memoryService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final MemoryQueryPort memoryService;

    private MemoryRetrievalNode(MemoryRetrievalConfig config, MemoryQueryPort service) {
        super(config != null ? config : new MemoryRetrievalConfig());
        this.config = config != null ? config : new MemoryRetrievalConfig();
        this.config.setNodeType(NodeType.MEMORY_RETRIEVAL);
        this.memoryService = service;
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
        private MemoryRetrievalConfig config;
        /**
         * memoryService 属性，保存当前对象中的业务数据或协作依赖。
         */
        private MemoryQueryPort memoryService;

        /**
         * {@code config} 执行当前类型定义的业务操作。
         *
         * @param c 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder config(MemoryRetrievalConfig c) {
            config = c;
            return this;
        }

        /**
         * {@code memoryService} 执行当前类型定义的业务操作。
         *
         * @param s 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder memoryService(MemoryQueryPort s) {
            memoryService = s;
            return this;
        }

        /**
         * {@code build} 执行当前类型定义的业务操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public MemoryRetrievalNode build() {
            if (memoryService == null)
                throw new IllegalStateException("memory service is required");
            return new MemoryRetrievalNode(config, memoryService);
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
    protected NodeOutput doExecute(NodeInput input) {
        String query = input.getParameter(FieldKey.QUERY, "");
        Long tenantId = input.getParameter(FieldKey.TENANT_ID, null);
        Long userId = input.getParameter(FieldKey.USER_ID, null);
        int topK =
                input.getParameter(FieldKey.TOP_K, config.getTopK() == null ? 5 : config.getTopK());
        NodeOutput output = new NodeOutput();
        try {
            if (tenantId == null || userId == null || userId <= 0)
                throw new IllegalArgumentException("tenantId and userId are required");
            List<MemoryPath> paths =
                    memoryService.retrieve(
                            new MemoryQuery(
                                    new TenantId(tenantId),
                                    "agent",
                                    "USER",
                                    String.valueOf(userId),
                                    query,
                                    Set.of(
                                            GraphType.SEMANTIC,
                                            GraphType.TEMPORAL,
                                            GraphType.CAUSAL,
                                            GraphType.ENTITY),
                                    null,
                                    null,
                                    2,
                                    topK,
                                    2000));
            List<Map<String, Object>> memories =
                    paths.stream()
                            .map(
                                    p -> {
                                        Map<String, Object> m = new LinkedHashMap<>();
                                        m.put("id", p.event().id());
                                        m.put("content", p.event().content());
                                        m.put("score", p.score());
                                        m.put("sourceType", p.event().sourceType());
                                        m.put("sourceId", p.event().sourceId());
                                        m.put("relations", p.edges());
                                        return m;
                                    })
                            .toList();
            output.setSuccess(true);
            output.setMsg("memory retrieval completed");
            output.addData(FieldKey.MEMORIES, memories);
            output.addData(FieldKey.MEMORY_COUNT, memories.size());
            output.addData(
                    FieldKey.MEMORY_CONTEXT,
                    memories.stream()
                            .map(m -> String.valueOf(m.get("content")))
                            .reduce("", (a, b) -> a.isBlank() ? b : a + "\n" + b));
            return output;
        } catch (Exception e) {
            log.error("memory retrieval failed", e);
            output.setSuccess(false);
            output.setMsg("记忆检索失败，请稍后重试");
            return output;
        }
    }

    /**
     * {@code getRequiredInputs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<NodeInputParam> getRequiredInputs() {
        return List.of(
                NodeInputParam.previous("tenantId", "number", "tenant scope"),
                NodeInputParam.previous("userId", "number", "user scope"),
                NodeInputParam.apiRequired("query", "string", "query"),
                NodeInputParam.config("topK", "number", "maximum results"));
    }
}
