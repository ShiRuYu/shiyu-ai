package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.memory.ShortTermMemoryConfig;
import com.shiyu.ai.agent.node.memory.ShortTermMemoryNode;
import com.shiyu.ai.memory.MemoryService;
import org.springframework.stereotype.Component;

@Component
public class ShortTermMemoryNodeCreator implements NodeCreator {
    private final MemoryService memoryService;
    public ShortTermMemoryNodeCreator(MemoryService memoryService) { this.memoryService = memoryService; }
    @Override public NodeType getType() { return NodeType.MEMORY_SHORT_TERM; }
    @Override public BaseNode create(NodeConfig config) {
        return ShortTermMemoryNode.builder().config((ShortTermMemoryConfig) config).memoryService(memoryService).build();
    }
}
