package com.shiyu.ai.agent.event;

import java.util.Map;

public class AgentExecutionCompletedEvent extends DomainEvent {

    private final String executionId;
    private final String agentId;
    private final Map<String, Object> output;
    private final long durationMs;

    public AgentExecutionCompletedEvent(String executionId, String agentId,
                                         Map<String, Object> output, long durationMs) {
        super("AGENT_EXECUTION_COMPLETED");
        this.executionId = executionId;
        this.agentId = agentId;
        this.output = output;
        this.durationMs = durationMs;
    }

    public String getExecutionId() { return executionId; }
    public String getAgentId() { return agentId; }
    public Map<String, Object> getOutput() { return output; }
    public long getDurationMs() { return durationMs; }
}
