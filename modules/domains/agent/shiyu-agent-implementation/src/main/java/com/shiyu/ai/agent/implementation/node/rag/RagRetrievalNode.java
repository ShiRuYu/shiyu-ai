package com.shiyu.ai.agent.implementation.node.rag;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRetrievalService;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalRequest;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalResult;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code RagRetrievalNode} 承载智能体模块中的智能流程节点，负责执行本节点的输入处理与结果产出。
 */
@Setter
@Getter
@Slf4j
public class RagRetrievalNode extends BaseNode {

    /**
     * 配置，表示当前对象中的对应属性。
     */
    private RagRetrievalConfig config;
    /**
     * retrievalService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRetrievalService retrievalService;

    private RagRetrievalNode(
            RagRetrievalConfig config, KnowledgeRetrievalService retrievalService) {
        super(config != null ? config : new RagRetrievalConfig());
        this.config = config != null ? config : new RagRetrievalConfig();
        this.config.setNodeType(NodeType.RAG_RETRIEVAL);
        this.retrievalService = retrievalService;
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
        private RagRetrievalConfig config;
        /**
         * retrievalService 属性，保存当前对象中的业务数据或协作依赖。
         */
        private KnowledgeRetrievalService retrievalService;

        /**
         * {@code config} 执行当前类型定义的业务操作。
         *
         * @param config 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder config(RagRetrievalConfig config) {
            this.config = config;
            return this;
        }

        /**
         * {@code retrievalService} 执行当前类型定义的业务操作。
         *
         * @param retrievalService 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder retrievalService(KnowledgeRetrievalService retrievalService) {
            this.retrievalService = retrievalService;
            return this;
        }

        /**
         * {@code build} 执行当前类型定义的业务操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public RagRetrievalNode build() {
            if (retrievalService == null) {
                throw new IllegalStateException("KnowledgeRetrievalService 不能为空");
            }
            return new RagRetrievalNode(config, retrievalService);
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
        if (query == null || query.isBlank()) {
            return NodeOutput.builder().success(false).msg("检索问题不能为空").build();
        }

        ActorContext context = input.getParameter("__knowledgeAccessContext");
        if (context == null) {
            throw new IllegalStateException("knowledge access context is required");
        }

        KnowledgeRetrievalRequest request =
                new KnowledgeRetrievalRequest(
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

        NodeOutput output =
                NodeOutput.builder()
                        .success(result.success())
                        .msg(result.success() ? "知识检索完成" : "知识检索失败，请稍后重试")
                        .build();
        output.addData(FieldKey.CONTEXT, result.context());
        output.addData("retrievalHits", result.hits());
        output.addData("citations", result.citations());
        output.addData("retrievalEmpty", result.hits().isEmpty());
        output.addData(
                FieldKey.DOCUMENTS,
                result.hits().stream()
                        .map(
                                hit -> {
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
                                    document.put(
                                            "score",
                                            hit.rerankScore() > 0
                                                    ? hit.rerankScore()
                                                    : hit.rrfScore());
                                    return document;
                                })
                        .toList());
        output.addData(FieldKey.DOCUMENT_COUNT, result.hits().size());
        return output;
    }

    /**
     * {@code getRequiredInputs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<NodeInputParam> getRequiredInputs() {
        return List.of(
                NodeInputParam.apiRequired("query", "string", "检索问题文本"),
                NodeInputParam.config("spaceIds", "array", "知识空间 ID；为空时检索所有有权限空间"),
                NodeInputParam.config("retrievalMode", "string", "KEYWORD、VECTOR 或 HYBRID"),
                NodeInputParam.config("topK", "number", "最终返回数量"),
                NodeInputParam.config("scoreThreshold", "number", "最低分数阈值"));
    }
}
