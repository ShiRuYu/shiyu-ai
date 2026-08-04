package com.shiyu.ai.agent.event;

import java.util.Map;

/**
 * 节点执行开始事件
 */
public class NodeExecutionStartedEvent extends DomainEvent {

    private final String executionId;
    private final String agentId;
    private final String nodeId;
    private final String nodeType;
    private final Map<String, Object> input;

    public NodeExecutionStartedEvent(String executionId, String agentId,
                                     String nodeId, String nodeType, Map<String, Object> input) {
        super("NODE_EXECUTION_STARTED");
        this.executionId = executionId;
        this.agentId = agentId;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.input = input;
    }

    public String getExecutionId() { return executionId; }
    public String getAgentId() { return agentId; }
    public String getNodeId() { return nodeId; }
    public String getNodeType() { return nodeType; }
    public Map<String, Object> getInput() { return input; }
}
