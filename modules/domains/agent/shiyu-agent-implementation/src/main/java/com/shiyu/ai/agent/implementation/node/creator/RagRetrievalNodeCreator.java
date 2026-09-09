package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.creator.NodeCreator;

import com.shiyu.ai.agent.contract.node.*;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.implementation.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.implementation.node.rag.RagRetrievalNode;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRetrievalService;
import org.springframework.stereotype.Component;

@Component
public class RagRetrievalNodeCreator implements NodeCreator {
    private final KnowledgeRetrievalService retrievalService;

    public RagRetrievalNodeCreator(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public NodeType getType() {
        return NodeType.RAG_RETRIEVAL;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        return RagRetrievalNode.builder()
                .config((RagRetrievalConfig) config)
                .retrievalService(retrievalService)
                .build();
    }
}

