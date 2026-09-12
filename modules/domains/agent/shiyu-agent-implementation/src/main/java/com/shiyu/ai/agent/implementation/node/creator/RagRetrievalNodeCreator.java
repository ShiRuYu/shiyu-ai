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
 * {@code RagRetrievalNodeCreator} 负责创建智能体模块中的运行时对象，并集中封装构造规则。
 */
@Component
public class RagRetrievalNodeCreator implements NodeCreator {
    /**
     * retrievalService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRetrievalService retrievalService;

    /**
     * {@code RagRetrievalNodeCreator} 创建并初始化当前类型实例。
     *
     * @param retrievalService 参数值，用于执行当前操作。
     */
    public RagRetrievalNodeCreator(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    /**
     * {@code getType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public NodeType getType() {
        return NodeType.RAG_RETRIEVAL;
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        return RagRetrievalNode.builder()
                .config((RagRetrievalConfig) config)
                .retrievalService(retrievalService)
                .build();
    }
}
