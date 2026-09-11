package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.agent.implementation.node.memory.ShortTermMemoryConfig;
import com.shiyu.ai.agent.implementation.node.memory.ShortTermMemoryNode;
import com.shiyu.ai.agent.implementation.runtime.AgentExecutionContext;

import org.springframework.stereotype.Component;

@Component
public class ShortTermMemoryNodeCreator implements NodeCreator {
    private final AgentExecutionContext memoryService;

    public ShortTermMemoryNodeCreator(AgentExecutionContext memoryService) {
        this.memoryService = memoryService;
    }

    @Override
    public NodeType getType() {
        return NodeType.MEMORY_SHORT_TERM;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        return ShortTermMemoryNode.builder()
                .config((ShortTermMemoryConfig) config)
                .memoryService(memoryService)
                .build();
    }
}
