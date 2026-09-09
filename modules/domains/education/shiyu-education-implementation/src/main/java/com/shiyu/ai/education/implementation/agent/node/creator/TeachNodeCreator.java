package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.education.implementation.agent.graph.TeachNode;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.model.contract.api.ChatEngine;
import org.springframework.stereotype.Component;

@Component
public class TeachNodeCreator implements NodeCreator {

    private final ChatEngine chatEngine;

    public TeachNodeCreator(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    @Override
    public NodeType getType() {
        return NodeType.EDUCATION_TEACH;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        TeachNode node = new TeachNode(chatEngine);
        node.setConfig(config);
        return node;
    }
}
