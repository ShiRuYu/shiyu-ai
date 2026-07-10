package com.shiyu.ai.agent.event;

import java.util.Map;

public class AgentExecutionStartedEvent extends DomainEvent {

    private final String executionId;
    private final String agentId;
    private final Map<String, Object> input;

    public AgentExecutionStartedEvent(String executionId, String agentId, Map<String, Object> input) {
        super("AGENT_EXECUTION_STARTED");
        this.executionId = executionId;
        this.agentId = agentId;
        this.input = input;
    }

    public String getExecutionId() { return executionId; }
    public String getAgentId() { return agentId; }
    public Map<String, Object> getInput() { return input; }
}
