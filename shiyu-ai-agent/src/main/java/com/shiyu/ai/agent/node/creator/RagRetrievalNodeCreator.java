package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.node.rag.RagRetrievalNode;
import com.shiyu.ai.knowledge.rag.RagService;
import org.springframework.stereotype.Component;

@Component
public class RagRetrievalNodeCreator implements NodeCreator {
    private final RagService ragService;
    public RagRetrievalNodeCreator(RagService ragService) { this.ragService = ragService; }
    @Override public NodeType getType() { return NodeType.RAG_RETRIEVAL; }
    @Override public BaseNode create(NodeConfig config) {
        return RagRetrievalNode.builder().config((RagRetrievalConfig) config).ragService(ragService).build();
    }
}
