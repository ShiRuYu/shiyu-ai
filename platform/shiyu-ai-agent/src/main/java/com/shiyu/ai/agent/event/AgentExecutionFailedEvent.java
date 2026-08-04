package com.shiyu.ai.agent.event;

public class AgentExecutionFailedEvent extends DomainEvent {

    private final String executionId;
    private final String agentId;
    private final String errorMessage;

    public AgentExecutionFailedEvent(String executionId, String agentId, String errorMessage) {
        super("AGENT_EXECUTION_FAILED");
        this.executionId = executionId;
        this.agentId = agentId;
        this.errorMessage = errorMessage;
    }

    public String getExecutionId() { return executionId; }
    public String getAgentId() { return agentId; }
    public String getErrorMessage() { return errorMessage; }
}
