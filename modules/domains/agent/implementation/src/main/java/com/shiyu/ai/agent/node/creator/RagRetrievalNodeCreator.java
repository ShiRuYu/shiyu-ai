package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.node.rag.RagRetrievalNode;
import com.shiyu.ai.knowledge.retrieval.KnowledgeRetrievalService;
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
