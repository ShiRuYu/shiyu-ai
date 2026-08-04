package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.node.llm.LlmCallNode;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.adapter.ModelManager;
import org.springframework.stereotype.Component;

@Component
public class LlmCallNodeCreator implements NodeCreator {
    private final ChatEngine chatEngine;
    private final ModelManager modelManager;
    public LlmCallNodeCreator(ChatEngine chatEngine, ModelManager modelManager) {
        this.chatEngine = chatEngine; this.modelManager = modelManager;
    }
    @Override public NodeType getType() { return NodeType.LLM_CALL; }
    @Override public BaseNode create(NodeConfig config) {
        return LlmCallNode.builder().config((LlmCallConfig) config).chatEngine(chatEngine).modelManager(modelManager).build();
    }
}
