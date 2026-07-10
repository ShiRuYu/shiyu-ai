package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.node.tool.ToolCallNode;
import com.shiyu.ai.tool.ToolService;
import org.springframework.stereotype.Component;

@Component
public class ToolCallNodeCreator implements NodeCreator {
    private final ToolService toolService;
    public ToolCallNodeCreator(ToolService toolService) { this.toolService = toolService; }
    @Override public NodeType getType() { return NodeType.TOOL_CALL; }
    @Override public BaseNode create(NodeConfig config) {
        return ToolCallNode.builder().config((ToolCallConfig) config).toolService(toolService).build();
    }
}
