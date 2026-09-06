package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.intent.IntentConfig;
import com.shiyu.ai.agent.node.intent.IntentNode;
import com.shiyu.ai.agent.service.IntentService;
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
