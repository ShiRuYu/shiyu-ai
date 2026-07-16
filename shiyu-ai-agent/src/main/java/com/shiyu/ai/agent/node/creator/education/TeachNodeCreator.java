package com.shiyu.ai.agent.node.creator.education;

import com.shiyu.ai.agent.education.graph.TeachNode;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.creator.NodeCreator;
import com.shiyu.ai.model.chat.ChatEngine;
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
