package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallNode;
import com.shiyu.ai.model.contract.api.ChatEngine;

import org.springframework.stereotype.Component;

@Component
public class LlmCallNodeCreator implements NodeCreator {
    private final ChatEngine chatEngine;

    public LlmCallNodeCreator(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    @Override
    public NodeType getType() {
        return NodeType.LLM_CALL;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        return LlmCallNode.builder().config((LlmCallConfig) config).chatEngine(chatEngine).build();
    }
}
