package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.creator.NodeCreator;

import com.shiyu.ai.agent.contract.node.*;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.implementation.node.intent.IntentConfig;
import com.shiyu.ai.agent.implementation.node.intent.IntentNode;
import com.shiyu.ai.agent.implementation.service.IntentService;
import org.springframework.stereotype.Component;

@Component
public class IntentNodeCreator implements NodeCreator {
    private final IntentService intentService;
    public IntentNodeCreator(IntentService intentService) { this.intentService = intentService; }
    @Override public NodeType getType() { return NodeType.INTENT; }
    @Override public BaseNode create(NodeConfig config) {
        return IntentNode.builder().config((IntentConfig) config).intentService(intentService).build();
    }
}
