package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.node.llm.LlmCallNode;
import com.shiyu.ai.model.chat.ChatEngine;
import org.springframework.stereotype.Component;

@Component
public class LlmCallNodeCreator implements NodeCreator {
    private final ChatEngine chatEngine;
    public LlmCallNodeCreator(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }
    @Override public NodeType getType() { return NodeType.LLM_CALL; }
    @Override public BaseNode create(NodeConfig config) {
        return LlmCallNode.builder().config((LlmCallConfig) config).chatEngine(chatEngine).build();
    }
}
