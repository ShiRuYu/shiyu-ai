package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;

public interface NodeCreator {
    NodeType getType();
    BaseNode create(NodeConfig config);
}
