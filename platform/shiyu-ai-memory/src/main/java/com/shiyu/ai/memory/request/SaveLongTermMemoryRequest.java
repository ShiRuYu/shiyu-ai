package com.shiyu.ai.memory.request;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 保存长期记忆请求参数
 */
public class SaveLongTermMemoryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String agentId;
    private String category;
    private String memoryKey;
    private String content;
    private double importance;
    private String source;

    public SaveLongTermMemoryRequest() {}

    public SaveLongTermMemoryRequest(Long userId, String agentId, String category,
                                     String memoryKey, String content, double importance, String source) {
        this.userId = userId;
        this.agentId = agentId;
        this.category = category;
        this.memoryKey = memoryKey;
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.importance = importance;
        this.source = source;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMemoryKey() { return memoryKey; }
    public void setMemoryKey(String memoryKey) { this.memoryKey = memoryKey; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public double getImportance() { return importance; }
    public void setImportance(double importance) { this.importance = importance; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long userId;
        private String agentId;
        private String category;
        private String memoryKey;
        private String content;
        private double importance;
        private String source;
        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder agentId(String v) { this.agentId = v; return this; }
        public Builder category(String v) { this.category = v; return this; }
        public Builder memoryKey(String v) { this.memoryKey = v; return this; }
        public Builder content(String v) { this.content = v; return this; }
        public Builder importance(double v) { this.importance = v; return this; }
        public Builder source(String v) { this.source = v; return this; }
        public SaveLongTermMemoryRequest build() {
            return new SaveLongTermMemoryRequest(userId, agentId, category, memoryKey, content, importance, source);
        }
    }
}
