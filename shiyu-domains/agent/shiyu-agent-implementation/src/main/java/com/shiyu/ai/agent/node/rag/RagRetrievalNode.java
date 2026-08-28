package com.shiyu.ai.agent.node.rag;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.knowledge.retrieval.KnowledgeRetrievalRequest;
import com.shiyu.ai.knowledge.retrieval.KnowledgeRetrievalResult;
import com.shiyu.ai.knowledge.retrieval.KnowledgeRetrievalService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
@Slf4j
public class RagRetrievalNode extends BaseNode {

    private RagRetrievalConfig config;
    private final KnowledgeRetrievalService retrievalService;

    private RagRetrievalNode(RagRetrievalConfig config, KnowledgeRetrievalService retrievalService) {
        super(config != null ? config : new RagRetrievalConfig());
        this.config = config != null ? config : new RagRetrievalConfig();
        this.config.setNodeType(NodeType.RAG_RETRIEVAL);
        this.retrievalService = retrievalService;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RagRetrievalConfig config;
        private KnowledgeRetrievalService retrievalService;

        public Builder config(RagRetrievalConfig config) {
            this.config = config;
            return this;
        }

        public Builder retrievalService(KnowledgeRetrievalService retrievalService) {
            this.retrievalService = retrievalService;
            return this;
        }

        public RagRetrievalNode build() {
            if (retrievalService == null) {
                throw new IllegalStateException("KnowledgeRetrievalService 不能为空");
            }
            return new RagRetrievalNode(config, retrievalService);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) {
        String query = input.getParameter(FieldKey.QUERY, "");
        if (query == null || query.isBlank()) {
            return NodeOutput.builder().success(false).msg("检索问题不能为空").build();
        }

        ActorContext context = input.getParameter("__knowledgeAccessContext");
        if (context == null) {
            throw new IllegalStateException("knowledge access context is required");
        }

        KnowledgeRetrievalRequest request = new KnowledgeRetrievalRequest(
                context,
                config.getSpaceIds(),
                config.getSourceTypes(),
                config.getRetrievalMode(),
                query,
                config.getCandidateTopK(),
                config.getTopK(),
                config.getScoreThreshold(),
                config.getEnableRerank());
        KnowledgeRetrievalResult result = retrievalService.retrieve(request);

        NodeOutput output = NodeOutput.builder()
                .success(result.success())
                .msg(result.success() ? "知识检索完成" : result.errorMessage())
                .build();
        output.addData(FieldKey.CONTEXT, result.context());
        output.addData("retrievalHits", result.hits());
        output.addData("citations", result.citations());
        output.addData("retrievalEmpty", result.hits().isEmpty());
        // RAG enhancement nodes consume the same hit list without a second retrieval service.
        output.addData(FieldKey.DOCUMENTS, result.hits().stream().map(hit -> {
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("spaceId", hit.spaceId());
            document.put("knowledgeId", hit.knowledgeId());
            document.put("documentId", hit.documentId());
            document.put("documentVersionId", hit.documentVersionId());
            document.put("chunkId", hit.chunkId());
            document.put("title", hit.title());
            document.put("content", hit.content());
            document.put("highlight", hit.highlight());
            document.put("pageNumber", hit.pageNumber());
            document.put("sectionPath", hit.sectionPath());
            document.put("score", hit.rerankScore() > 0 ? hit.rerankScore() : hit.rrfScore());
            return document;
        }).toList());
        output.addData(FieldKey.DOCUMENT_COUNT, result.hits().size());
        return output;
    }

    @Override
    public List<NodeInputParam> getRequiredInputs() {
        return List.of(
                NodeInputParam.apiRequired("query", "string", "检索问题文本"),
                NodeInputParam.config("spaceIds", "array", "知识空间 ID；为空时检索所有有权限空间"),
                NodeInputParam.config("retrievalMode", "string", "KEYWORD、VECTOR 或 HYBRID"),
                NodeInputParam.config("topK", "number", "最终返回数量"),
                NodeInputParam.config("scoreThreshold", "number", "最低分数阈值")
        );
    }
}
