package com.shiyu.ai.agent.contract.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;

/** Factory boundary that turns persisted node configuration into an executable node. */
public interface NodeCreator {
    /** Returns the node type handled by this creator. */
    NodeType getType();

    /** Creates a node from validated configuration. */
    BaseNode create(NodeConfig config);
}
