package com.shiyu.ai.agent.node.rag;

import com.shiyu.ai.knowledge.rag.RagService;
import com.shiyu.ai.knowledge.search.SearchSource;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import com.shiyu.ai.agent.node.NodeInputParam;

@Setter
@Getter
@Slf4j
public class RagRetrievalNode extends BaseNode {

    private RagRetrievalConfig config;

    private final RagService ragService;

    private RagRetrievalNode(RagRetrievalConfig config, RagService ragService) {
        super(config != null ? config : new RagRetrievalConfig());
        this.config = config != null ? config : new RagRetrievalConfig();
        this.config.setNodeType(NodeType.RAG_RETRIEVAL);
        this.ragService = ragService;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RagRetrievalConfig config;
        private RagService ragService;

        public Builder config(RagRetrievalConfig config) {
            this.config = config;
            return this;
        }

        public Builder ragService(RagService ragService) {
            this.ragService = ragService;
            return this;
        }

        public RagRetrievalNode build() {
            if (ragService == null) {
                throw new IllegalStateException("创建 RagRetrievalNode 失败: ragService 不能为空");
            }
            return new RagRetrievalNode(config, ragService);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 RAG 检索节点: {}", config.getNodeName());

        try {
            // 1. 获取检索参数
            String query = input.getParameter(FieldKey.QUERY, "");
            if (query == null || query.trim().isEmpty()) {
                log.warn("检索查询为空，跳过 RAG 检索");
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("检索查询为空");
                return output;
            }

            // 2. 调用 RAG 检索服务
            SearchSource source = getSearchSource(input);
            int topK = input.getParameter(FieldKey.TOP_K, config.getTopK() != null ? config.getTopK() : 5);
            
            RagService.RagRetrievalResult result = 
                    ragService.retrieve(query, source, topK);
            
            // 3. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(result.success());
            output.setMsg("RAG 检索完成");
            
            if (result.success() && result.documents() != null && !result.documents().isEmpty()) {
                String context = buildContextFromDocuments(result.documents());
                output.addData(FieldKey.CONTEXT, context);
                output.addData(FieldKey.DOCUMENTS, result.documents());
                log.info("RAG 检索成功，返回 {} 条文档", result.documents().size());
            } else {
                log.warn("RAG 检索无结果");
                output.addData(FieldKey.CONTEXT, "");
                output.addData(FieldKey.DOCUMENTS, List.of());
            }

            return output;

        } catch (Exception e) {
            log.error("RAG 检索节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("RAG 检索失败: " + e.getMessage());
            return output;
        }
    }

    /**
     * 获取检索来源
     */
    private SearchSource getSearchSource(NodeInput input) {
        String sourceStr = input.getParameter(FieldKey.KNOWLEDGE_BASE_ID, "");
        if (sourceStr == null || sourceStr.trim().isEmpty()) {
            sourceStr = config.getKnowledgeBaseId();
        }
        if (sourceStr == null || sourceStr.trim().isEmpty()) {
            return SearchSource.DOCUMENT;
        }
        // 兼容旧配置：将 knowledge/document 映射为枚举
        return switch (sourceStr.trim().toLowerCase()) {
            case "knowledge", "kp", "知识点" -> SearchSource.KNOWLEDGE;
            default -> SearchSource.DOCUMENT;
        };
    }

    private String buildContextFromDocuments(List<RagService.Document> documents) {
        if (documents == null || documents.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("检索结果:\n\n");
        for (RagService.Document doc : documents) {
            sb.append("---\n");
            sb.append(doc.content()).append("\n");
            if (doc.metadata() != null && !doc.metadata().isEmpty()) {
                sb.append("元数据: ").append(doc.metadata()).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.apiRequired("query", "string", "检索查询文本"),
            NodeInputParam.config("knowledgeBaseId", "string", "知识库 ID（knowledge/document）"),
            NodeInputParam.config("topK", "number", "最大检索结果数"),
            NodeInputParam.config("similarityThreshold", "number", "相似度阈值")
        );
    }
}
