package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.agent.AgentCallConfig;
import com.shiyu.ai.agent.node.agent.AgentCallNode;
import com.shiyu.ai.agent.runtime.AgentRuntime;
import org.springframework.stereotype.Component;

@Component
public class AgentCallNodeCreator implements NodeCreator {
    private final AgentRuntime agentRuntime;
    public AgentCallNodeCreator(@org.springframework.context.annotation.Lazy AgentRuntime agentRuntime) {
        this.agentRuntime = agentRuntime;
    }
    @Override public NodeType getType() { return NodeType.AGENT_CALL; }
    @Override public BaseNode create(NodeConfig config) {
        return AgentCallNode.builder()
                .config((AgentCallConfig) config)
                .agentRuntime(agentRuntime)
                .build();
    }
}
