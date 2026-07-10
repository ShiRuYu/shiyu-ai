package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.agent.AgentCallConfig;
import com.shiyu.ai.agent.node.agent.AgentCallNode;
import com.shiyu.ai.agent.service.AgentService;
import org.springframework.stereotype.Component;

@Component
public class AgentCallNodeCreator implements NodeCreator {
    private final AgentService agentService;
    public AgentCallNodeCreator(@org.springframework.context.annotation.Lazy AgentService agentService) { this.agentService = agentService; }
    @Override public NodeType getType() { return NodeType.AGENT_CALL; }
    @Override public BaseNode create(NodeConfig config) {
        return AgentCallNode.builder().config((AgentCallConfig) config).agentService(agentService).build();
    }
}
