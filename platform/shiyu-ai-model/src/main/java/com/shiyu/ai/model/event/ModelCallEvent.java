package com.shiyu.ai.model.event;

/**
 * 模型调用事件
 * <p>
 * 每次 LLM 对话调用完成后发布，携带 Token 用量、平台、模型等信息。
 * 由 {@code shiyu-ai-usage} 模块监听并记录用量。
 * </p>
 */
public class ModelCallEvent {

    private final String platform;
    private final String model;
    private final int promptTokens;
    private final int completionTokens;
    private final long latencyMs;
    private final String generationRunId;

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs) {
        this(platform, model, promptTokens, completionTokens, latencyMs, null);
    }

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs, String generationRunId) {
        this.platform = platform;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
        this.generationRunId = generationRunId;
    }

    public String getPlatform() { return platform; }
    public String getModel() { return model; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public int getTotalTokens() { return promptTokens + completionTokens; }
    public long getLatencyMs() { return latencyMs; }
    public String getGenerationRunId() { return generationRunId; }
}
