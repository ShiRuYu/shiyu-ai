package com.shiyu.ai.memory.spi;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Memory {

    private String memoryId;
    private MemoryType type;
    private String sessionId;
    private Long userId;
    private String agentId;
    private String role;
    private String content;
    private String category;
    private String memoryKey;
    private double importance = 0.5;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime accessedAt;
    private int accessCount;
    private Map<String, Object> metadata;

    public Memory() {
        this.metadata = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.accessedAt = LocalDateTime.now();
    }

    public Memory(MemoryType type, String sessionId, String role, String content) {
        this();
        this.type = type;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
    }

    // Getters & Setters
    public String getMemoryId() { return memoryId; }
    public void setMemoryId(String memoryId) { this.memoryId = memoryId; }
    public MemoryType getType() { return type; }
    public void setType(MemoryType type) { this.type = type; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMemoryKey() { return memoryKey; }
    public void setMemoryKey(String memoryKey) { this.memoryKey = memoryKey; }
    public double getImportance() { return importance; }
    public void setImportance(double importance) { this.importance = importance; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getAccessedAt() { return accessedAt; }
    public void setAccessedAt(LocalDateTime accessedAt) { this.accessedAt = accessedAt; }
    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
