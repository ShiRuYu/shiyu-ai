package com.shiyu.ai.agent.langgraph4j.node.memory;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.langgraph4j.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 记忆检索节点
 * 用于从记忆中检索相关信息
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
public class MemoryRetrievalNode extends BaseNode {

    private MemoryRetrievalConfig config;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     */
    private MemoryRetrievalNode(MemoryRetrievalConfig config) {
        super(config != null ? config : new MemoryRetrievalConfig());
        this.config = config != null ? config : new MemoryRetrievalConfig();
        // 设置节点类型为 MEMORY_RETRIEVAL
        this.config.setNodeType(NodeType.MEMORY_RETRIEVAL);
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 MemoryRetrievalNode 实例
     */
    public static class Builder {
        private MemoryRetrievalConfig config;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(MemoryRetrievalConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返回 MemoryRetrievalNode 实例
         * @return MemoryRetrievalNode 实例
         */
        public MemoryRetrievalNode build() {
            return new MemoryRetrievalNode(config);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行记忆检索节点：{}", config.getNodeName());
        log.debug("检索配置：retrievalScope={}, topK={}, similarityThreshold={}", 
                config.getRetrievalScope(), config.getTopK(), config.getSimilarityThreshold());
        
        try {
            // 1. 获取查询文本
            String query = input.getParameter(FieldKey.QUERY, "");

            // 2. 从输入中获取参数
            String retrievalScope = input.getParameter(FieldKey.RETRIEVAL_SCOPE,
                    config.getRetrievalScope() != null ? config.getRetrievalScope() : "SHORT_TERM");
            Integer topK = input.getParameter(FieldKey.TOP_K,
                    config.getTopK() != null ? config.getTopK() : 5);
            Double similarityThreshold = input.getParameter(FieldKey.SIMILARITY_THRESHOLD,
                    config.getSimilarityThreshold() != null ? config.getSimilarityThreshold() : 0.7);
            
            // 3. 调用记忆检索服务（这里使用示例实现）
            java.util.List<Map<String, Object>> memories = mockMemoryRetrieval(query, topK, similarityThreshold);
            
            // 4. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("记忆检索成功");
            output.addData(FieldKey.MEMORIES, memories);
            output.addData(FieldKey.MEMORY_COUNT, memories.size());

            // 将相关记忆添加到上下文中
            if (!memories.isEmpty()) {
                String context = buildMemoryContext(memories);
                output.addData(FieldKey.MEMORY_CONTEXT, context);
            }
            
            log.info("记忆检索成功，返回 {} 条记忆", memories.size());
            return output;
            
        } catch (Exception e) {
            log.error("记忆检索节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("记忆检索节点执行失败：" + e.getMessage());
            return output;
        }
    }
    
    /**
     * 模拟记忆检索（实际项目需要接入向量数据库）
     */
    private java.util.List<Map<String, Object>> mockMemoryRetrieval(String query, int topK, double threshold) {
        java.util.List<Map<String, Object>> memories = new java.util.ArrayList<>();
        
        // 示例数据
        for (int i = 0; i < Math.min(topK, 3); i++) {
            memories.add(java.util.Map.of(
                "id", "memory_" + i,
                "content", "这是第 " + (i + 1) + " 条相关记忆，查询：" + query,
                "score", 0.95 - i * 0.1,
                "timestamp", System.currentTimeMillis() - i * 1000000L,
                "type", "conversation"
            ));
        }
        
        return memories;
    }
    
    /**
     * 构建记忆上下文
     */
    private String buildMemoryContext(java.util.List<Map<String, Object>> memories) {
        StringBuilder context = new StringBuilder();
        context.append("相关记忆：\n\n");
        
        for (int i = 0; i < memories.size(); i++) {
            Map<String, Object> memory = memories.get(i);
            context.append("[记忆 ").append(i + 1).append("] ");
            context.append(memory.get("content")).append("\n");
        }
        
        return context.toString();
    }
}
