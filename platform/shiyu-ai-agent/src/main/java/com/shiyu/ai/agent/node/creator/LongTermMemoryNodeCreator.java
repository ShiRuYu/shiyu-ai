package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.memory.LongTermMemoryConfig;
import com.shiyu.ai.agent.node.memory.LongTermMemoryNode;
import com.shiyu.ai.memory.MemoryService;
import org.springframework.stereotype.Component;

@Component
public class LongTermMemoryNodeCreator implements NodeCreator {
    private final MemoryService memoryService;
    public LongTermMemoryNodeCreator(MemoryService memoryService) { this.memoryService = memoryService; }
    @Override public NodeType getType() { return NodeType.MEMORY_LONG_TERM; }
    @Override public BaseNode create(NodeConfig config) {
        return LongTermMemoryNode.builder().config((LongTermMemoryConfig) config).memoryService(memoryService).build();
    }
}
