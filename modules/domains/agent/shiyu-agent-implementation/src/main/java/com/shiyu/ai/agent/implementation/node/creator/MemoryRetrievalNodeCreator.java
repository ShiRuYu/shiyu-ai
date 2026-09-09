package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.creator.NodeCreator;

import com.shiyu.ai.agent.contract.node.*;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.implementation.node.memory.MemoryRetrievalConfig;
import com.shiyu.ai.agent.implementation.node.memory.MemoryRetrievalNode;
import com.shiyu.ai.memory.contract.api.MemoryQueryPort;
import org.springframework.stereotype.Component;

@Component
public class MemoryRetrievalNodeCreator implements NodeCreator {
    private final MemoryQueryPort memoryService;
    public MemoryRetrievalNodeCreator(MemoryQueryPort memoryService) { this.memoryService = memoryService; }
    @Override public NodeType getType() { return NodeType.MEMORY_RETRIEVAL; }
    @Override public BaseNode create(NodeConfig config) {
        return MemoryRetrievalNode.builder().config((MemoryRetrievalConfig) config).memoryService(memoryService).build();
    }
}

