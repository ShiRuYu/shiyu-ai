package com.shiyu.ai.agent.node.creator;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.node.tool.ToolCallNode;
import com.shiyu.ai.tool.ToolService;
import com.shiyu.ai.runtime.ToolExecutionPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToolCallNodeCreator implements NodeCreator {
    private final ToolService toolService;
    private final ToolExecutionPipeline executionPipeline;
    /**
     * Explicitly select the full constructor.  This creator has two constructors
     * because a few unit tests instantiate it with only the tool service; without
     * an explicit injection marker Spring treats the class as requiring a default
     * constructor and the application fails during bootstrap.
     */
    public ToolCallNodeCreator(ToolService toolService) { this(toolService, null); }
    @Autowired
    public ToolCallNodeCreator(ToolService toolService, ToolExecutionPipeline executionPipeline) {
        this.toolService = toolService;
        this.executionPipeline = executionPipeline;
    }
    @Override public NodeType getType() { return NodeType.TOOL_CALL; }
    @Override public BaseNode create(NodeConfig config) {
        return ToolCallNode.builder().config((ToolCallConfig) config).toolService(toolService)
                .executionPipeline(executionPipeline).build();
    }
}
