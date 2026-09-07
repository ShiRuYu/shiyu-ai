package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.memory.MemoryRetrievalConfig;
import com.shiyu.ai.agent.node.memory.MemoryRetrievalNode;
import com.shiyu.ai.memory.magma.MemoryQueryPort;
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
