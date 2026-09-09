package com.shiyu.ai.agent.contract.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;

public interface NodeCreator {
    NodeType getType();
    BaseNode create(NodeConfig config);
}
