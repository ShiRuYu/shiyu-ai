package com.shiyu.ai.memory.request;

import com.shiyu.ai.memory.spi.MemoryType;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 记忆检索请求参数，支持跨多种记忆类型统一检索
 */
public class RetrieveMemoryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String query;
    private String sessionId;
    private Long userId;
    private String agentId;
    private String keyword;
    private String category;
    private int topK = 10;
    private double minImportance = 0.0;
    private List<MemoryType> types;

    public RetrieveMemoryRequest() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
    public double getMinImportance() { return minImportance; }
    public void setMinImportance(double minImportance) { this.minImportance = minImportance; }
    public List<MemoryType> getTypes() { return types; }
    public void setTypes(List<MemoryType> types) { this.types = types; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RetrieveMemoryRequest req = new RetrieveMemoryRequest();
        public Builder query(String v) { req.query = v; return this; }
        public Builder sessionId(String v) { req.sessionId = v; return this; }
        public Builder userId(Long v) { req.userId = v; return this; }
        public Builder agentId(String v) { req.agentId = v; return this; }
        public Builder keyword(String v) { req.keyword = v; return this; }
        public Builder category(String v) { req.category = v; return this; }
        public Builder topK(int v) { req.topK = v; return this; }
        public Builder minImportance(double v) { req.minImportance = v; return this; }
        public Builder types(List<MemoryType> v) { req.types = v; return this; }
        public RetrieveMemoryRequest build() { return req; }
    }
}
