package com.shiyu.ai.agent.langgraph4j.node.rag;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.agent.service.RagService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RAG 检索节点
 * 用于从知识库中检索相关信息
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
public class RagRetrievalNode extends BaseNode {

    private RagRetrievalConfig config;
    
    /**
     * RAG 检索服务（必须依赖）
     */
    private final RagService ragService;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     * @param ragService RAG 检索服务
     */
    private RagRetrievalNode(RagRetrievalConfig config, RagService ragService) {
        super(config != null ? config : new RagRetrievalConfig());
        this.config = config != null ? config : new RagRetrievalConfig();
        // 设置节点类型为 RAG_RETRIEVAL
        this.config.setNodeType(NodeType.RAG_RETRIEVAL);
        this.ragService = ragService;
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 RagRetrievalNode 实例
     */
    public static class Builder {
        private RagRetrievalConfig config;
        private RagService ragService;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(RagRetrievalConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置 RAG 检索服务
         * @param ragService RAG 检索服务
         * @return Builder 实例
         */
        public Builder ragService(RagService ragService) {
            this.ragService = ragService;
            return this;
        }

        /**
         * 构建并返回 RagRetrievalNode 实例
         * 在构建前会进行必要的校验
         * @return RagRetrievalNode 实例
         * @throws IllegalStateException 如果校验失败
         */
        public RagRetrievalNode build() {
            // 校验：ragService 不能为空
            if (ragService == null) {
                throw new IllegalStateException("创建 RagRetrievalNode 失败：ragService 不能为空");
            }
            
            // 所有校验通过，创建并返回实例
            return new RagRetrievalNode(config, ragService);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 RAG 检索节点：{}", config.getNodeName());
        log.debug("检索配置：knowledgeBaseId={}, topK={}, strategy={}", 
                config.getKnowledgeBaseId(), config.getTopK(), config.getRetrievalStrategy());
        
        try {
            // 1. 获取查询文本
            String query = input.getParameter("query", "");
            if (query == null || query.trim().isEmpty()) {
                query = input.getParameter("userInput", "");
            }
            
            // 2. 调用 RAG 检索服务
            String knowledgeBaseId = getKnowledgeBaseId(input);
            int topK = config.getTopK() != null ? config.getTopK() : 5;
            
            RagService.RagRetrievalResult result = 
                    ragService.retrieve(query, knowledgeBaseId, topK);
            
            // 3. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(result.success());
            output.setMsg(result.errorMessage() != null ? result.errorMessage() : "RAG 检索成功");
            
            if (result.success()) {
                // 添加检索到的文档到输出
                List<Map<String, Object>> documentsList = new ArrayList<>();
                for (RagService.Document doc : result.documents()) {
                    documentsList.add(Map.of(
                        "id", doc.id(),
                        "content", doc.content(),
                        "score", doc.score(),
                        "metadata", doc.metadata()
                    ));
                }
                
                output.addData("documents", documentsList);
                output.addData("documentCount", documentsList.size());
                
                // 将文档内容合并为上下文（供 LLM 使用）
                String context = buildContextFromDocuments(result.documents());
                output.addData("context", context);
                
                log.info("RAG 检索成功，返回 {} 个文档", documentsList.size());
            } else {
                log.warn("RAG 检索失败：{}", result.errorMessage());
            }
            
            log.info("RAG 检索节点执行完成");
            return output;
            
        } catch (Exception e) {
            log.error("RAG 检索节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("RAG 检索节点执行失败：" + e.getMessage());
            return output;
        }
    }
    
    /**
     * 从文档构建上下文
     */
    private String buildContextFromDocuments(List<RagService.Document> documents) {
        StringBuilder context = new StringBuilder();
        context.append("相关文档信息：\n\n");
        
        for (int i = 0; i < documents.size(); i++) {
            RagService.Document doc = documents.get(i);
            context.append("[文档 ").append(i + 1).append("]\n");
            context.append(doc.content()).append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * 获取知识库 ID
     */
    private String getKnowledgeBaseId(NodeInput input) {
        // 优先使用输入中的配置
        String kbId = input.getParameter("knowledgeBaseId", "");
        if (kbId != null && !kbId.trim().isEmpty()) {
            return kbId;
        }
        
        // 使用节点配置
        return config.getKnowledgeBaseId();
    }
}
