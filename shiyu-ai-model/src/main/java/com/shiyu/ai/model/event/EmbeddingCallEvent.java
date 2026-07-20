package com.shiyu.ai.model.event;

/**
 * 嵌入向量化调用事件
 * <p>
 * 每次 Embedding 向量化完成后发布，携带文本长度、Token 估算等用量信息。
 * 由 {@code shiyu-ai-usage} 模块监听并记录用量。
 * </p>
 */
public class EmbeddingCallEvent {

    private final String model;
    private final int textLength;
    private final int estimatedTokens;
    private final int vectorCount;
    private final long latencyMs;

    public EmbeddingCallEvent(String model, int textLength,
                              int estimatedTokens, int vectorCount, long latencyMs) {
        this.model = model;
        this.textLength = textLength;
        this.estimatedTokens = estimatedTokens;
        this.vectorCount = vectorCount;
        this.latencyMs = latencyMs;
    }

    public String getModel() { return model; }
    public int getTextLength() { return textLength; }
    public int getEstimatedTokens() { return estimatedTokens; }
    public int getVectorCount() { return vectorCount; }
    public long getLatencyMs() { return latencyMs; }
}
