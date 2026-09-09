package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.creator.NodeCreator;

import com.shiyu.ai.agent.contract.node.*;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.implementation.node.memory.LongTermMemoryConfig;
import com.shiyu.ai.agent.implementation.node.memory.LongTermMemoryNode;
import com.shiyu.ai.memory.contract.api.MemoryIngestionPort;
import org.springframework.stereotype.Component;

@Component
public class LongTermMemoryNodeCreator implements NodeCreator {
    private final MemoryIngestionPort memoryService;
    public LongTermMemoryNodeCreator(MemoryIngestionPort memoryService) { this.memoryService = memoryService; }
    @Override public NodeType getType() { return NodeType.MEMORY_LONG_TERM; }
    @Override public BaseNode create(NodeConfig config) {
        return LongTermMemoryNode.builder().config((LongTermMemoryConfig) config).memoryService(memoryService).build();
    }
}

