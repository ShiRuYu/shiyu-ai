package com.shiyu.ai.agent.langgraph4j.node.memory;

import com.shiyu.ai.agent.biz.agent.service.MemoryService;
import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.langgraph4j.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            List<Map<String, Object>> allMemories = new ArrayList<>();

            if ("SHORT_TERM".equalsIgnoreCase(retrievalScope) || "BOTH".equalsIgnoreCase(retrievalScope) || "ALL".equalsIgnoreCase(retrievalScope)) {
                if (sessionId != null && !sessionId.isEmpty()) {
                    List<Map<String, Object>> shortTerm = memoryService.retrieveShortTerm(sessionId, topK);
                    for (Map<String, Object> m : shortTerm) {
                        m.put("type", "short_term");
                    }
                    allMemories.addAll(shortTerm);
                    log.debug("短期记忆检索到 {} 条", shortTerm.size());
                }
            }

            if ("LONG_TERM".equalsIgnoreCase(retrievalScope) || "BOTH".equalsIgnoreCase(retrievalScope) || "ALL".equalsIgnoreCase(retrievalScope)) {
                List<Map<String, Object>> longTerm = memoryService.retrieveLongTerm(query, userId, agentId, topK);
                for (Map<String, Object> m : longTerm) {
                    m.put("type", "long_term");
                }
                allMemories.addAll(longTerm);
                log.debug("长期记忆检索到 {} 条", longTerm.size());
            }

            String context = buildMemoryContext(allMemories, similarityThreshold);

            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("记忆检索成功");
            output.addData(FieldKey.MEMORIES, allMemories);
            output.addData(FieldKey.MEMORY_COUNT, allMemories.size());

            if (!allMemories.isEmpty()) {
                output.addData(FieldKey.MEMORY_CONTEXT, context);
            }

            log.info("记忆检索成功, 返回 {} 条记忆 (范围: {})", allMemories.size(), retrievalScope);
            return output;

        } catch (Exception e) {
            log.error("记忆检索节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("记忆检索节点执行失败: " + e.getMessage());
            return output;
        }
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
}
