package com.shiyu.ai.agent.event;

public class ModelCallEvent extends DomainEvent {

    private final String platform;
    private final String model;
    private final int promptTokens;
    private final int completionTokens;
    private final long latencyMs;

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs) {
        super("MODEL_CALL");
        this.platform = platform;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
    }

    public String getPlatform() { return platform; }
    public String getModel() { return model; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public long getLatencyMs() { return latencyMs; }
}
