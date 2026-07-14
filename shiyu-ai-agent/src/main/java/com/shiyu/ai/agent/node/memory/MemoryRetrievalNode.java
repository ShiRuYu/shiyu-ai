package com.shiyu.ai.agent.node.memory;

import com.shiyu.ai.memory.MemoryService;
import com.shiyu.ai.memory.request.RetrieveMemoryRequest;
import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryType;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.shiyu.ai.agent.node.NodeInputParam;

@Setter
@Getter
@Slf4j
public class MemoryRetrievalNode extends BaseNode {

    private MemoryRetrievalConfig config;

    private final MemoryService memoryService;

    private MemoryRetrievalNode(MemoryRetrievalConfig config, MemoryService memoryService) {
        super(config != null ? config : new MemoryRetrievalConfig());
        this.config = config != null ? config : new MemoryRetrievalConfig();
        this.config.setNodeType(NodeType.MEMORY_RETRIEVAL);
        this.memoryService = memoryService;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MemoryRetrievalConfig config;
        private MemoryService memoryService;

        public Builder config(MemoryRetrievalConfig config) {
            this.config = config;
            return this;
        }

        public Builder memoryService(MemoryService memoryService) {
            this.memoryService = memoryService;
            return this;
        }

        public MemoryRetrievalNode build() {
            if (memoryService == null) {
                throw new IllegalStateException("创建 MemoryRetrievalNode 失败: memoryService 不能为空");
            }
            return new MemoryRetrievalNode(config, memoryService);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行记忆检索节点: {}", config.getNodeName());

        try {
            String query = input.getParameter(FieldKey.QUERY, "");
            String sessionId = input.getParameter(FieldKey.SESSION_ID, "");
            Long userId = input.getParameter(FieldKey.USER_ID, null);
            String agentId = input.getParameter(FieldKey.AGENT_ID, "");

            String retrievalScope = input.getParameter(FieldKey.RETRIEVAL_SCOPE,
                    config.getRetrievalScope() != null ? config.getRetrievalScope() : "ALL");
            int topK = input.getParameter(FieldKey.TOP_K,
                    config.getTopK() != null ? config.getTopK() : 5);
            double similarityThreshold = input.getParameter(FieldKey.SIMILARITY_THRESHOLD,
                    config.getSimilarityThreshold() != null ? config.getSimilarityThreshold() : 0.5);

            List<Memory> allMemories = new ArrayList<>();

            if ("SHORT_TERM".equalsIgnoreCase(retrievalScope) || "BOTH".equalsIgnoreCase(retrievalScope) || "ALL".equalsIgnoreCase(retrievalScope)) {
                if (sessionId != null && !sessionId.isEmpty()) {
                    List<Memory> shortTerm = memoryService.retrieveShortTerm(sessionId, topK);
                    allMemories.addAll(shortTerm);
                    log.debug("短期记忆检索到 {} 条", shortTerm.size());
                }
            }

            if ("LONG_TERM".equalsIgnoreCase(retrievalScope) || "BOTH".equalsIgnoreCase(retrievalScope) || "ALL".equalsIgnoreCase(retrievalScope)) {
                // 使用统一检索接口
                RetrieveMemoryRequest retrievalRequest = RetrieveMemoryRequest.builder()
                        .query(query)
                        .userId(userId)
                        .agentId(agentId)
                        .topK(topK)
                        .build();
                List<Memory> longTerm = memoryService.retrieve(retrievalRequest);
                allMemories.addAll(longTerm);
                log.debug("长期/语义记忆检索到 {} 条", longTerm.size());
            }

            List<Map<String, Object>> memoryMaps = memoriesToMapList(allMemories);
            String context = buildMemoryContext(memoryMaps, similarityThreshold);

            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("记忆检索成功");
            output.addData(FieldKey.MEMORIES, memoryMaps);
            output.addData(FieldKey.MEMORY_COUNT, memoryMaps.size());

            if (!memoryMaps.isEmpty()) {
                output.addData(FieldKey.MEMORY_CONTEXT, context);
            }

            log.info("记忆检索成功, 返回 {} 条记忆 (范围: {})", memoryMaps.size(), retrievalScope);
            return output;

        } catch (Exception e) {
            log.error("记忆检索节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("记忆检索节点执行失败: " + e.getMessage());
            return output;
        }
    }

    private List<Map<String, Object>> memoriesToMapList(List<Memory> memories) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Memory mem : memories) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", mem.getMemoryId());
            map.put("content", mem.getContent());
            map.put("category", mem.getCategory());
            map.put("importance", mem.getImportance());
            map.put("memoryKey", mem.getMemoryKey());
            map.put("type", typeToScope(mem.getType()));
            map.put("timestamp", mem.getCreatedAt());
            map.put("role", mem.getRole());
            map.put("sessionId", mem.getSessionId());
            result.add(map);
        }
        return result;
    }

    private String typeToScope(MemoryType type) {
        if (type == null) return "unknown";
        return switch (type) {
            case SHORT_TERM -> "short_term";
            case LONG_TERM, SEMANTIC -> "long_term";
            case WORKING -> "working";
            case EPISODIC -> "episodic";
        };
    }

    private String buildMemoryContext(List<Map<String, Object>> memories, double threshold) {
        if (memories.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("相关记忆:\n\n");

        int index = 0;
        for (Map<String, Object> mem : memories) {
            String type = (String) mem.getOrDefault("type", "unknown");
            String content = (String) mem.getOrDefault("content", "");
            if (content.isBlank()) continue;

            if (threshold > 0 && mem.containsKey("score")) {
                Object scoreObj = mem.get("score");
                double score = 0;
                if (scoreObj instanceof Number n) score = n.doubleValue();
                if (score < threshold) continue;
            }

            index++;
            sb.append("[").append(index).append("] (").append(type).append(") ");
            sb.append(content).append("\n\n");
        }

        return sb.toString();
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.apiRequired("query", "string", "检索查询"),
            NodeInputParam.previous("sessionId", "string", "会话 ID"),
            NodeInputParam.config("retrievalScope", "string", "检索范围（session/user/global）"),
            NodeInputParam.config("topK", "number", "最大返回数量"),
            NodeInputParam.config("similarityThreshold", "number", "相似度阈值")
        );
    }
}
