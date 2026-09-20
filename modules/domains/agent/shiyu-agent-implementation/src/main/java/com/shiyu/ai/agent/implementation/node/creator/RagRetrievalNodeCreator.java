package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.agent.implementation.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.implementation.node.rag.RagRetrievalNode;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRetrievalService;

import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 Rag Retrieval Node 相关的流程节点或业务组件。
 */
@Component
public class RagRetrievalNodeCreator implements NodeCreator {
    /**
     * retrievalService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRetrievalService retrievalService;

    /**
     * 执行 Rag Retrieval Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param retrievalService 用于完成本次业务处理的 retrievalService 参数。
     */
    public RagRetrievalNodeCreator(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    /**
     * 查询 Rag Retrieval Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Rag Retrieval Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return NodeType.RAG_RETRIEVAL;
    }

    /**
     * 创建或保存 Rag Retrieval Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Rag Retrieval Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        return RagRetrievalNode.builder()
                .config((RagRetrievalConfig) config)
                .retrievalService(retrievalService)
                .build();
    }
}
