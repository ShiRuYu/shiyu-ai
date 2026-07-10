package com.shiyu.ai.memory.spi;

import java.util.ArrayList;
import java.util.List;

public class MemoryQuery {

    private String sessionId;
    private Long userId;
    private String agentId;
    private MemoryType type;
    private String category;
    private String keyword;
    private int topK = 10;
    private double minImportance = 0.0;
    private List<MemoryType> types;

    public static MemoryQueryBuilder builder() {
        return new MemoryQueryBuilder();
    }

    public static class MemoryQueryBuilder {
        private final MemoryQuery query = new MemoryQuery();

        public MemoryQueryBuilder sessionId(String sessionId) { query.sessionId = sessionId; return this; }
        public MemoryQueryBuilder userId(Long userId) { query.userId = userId; return this; }
        public MemoryQueryBuilder agentId(String agentId) { query.agentId = agentId; return this; }
        public MemoryQueryBuilder type(MemoryType type) { query.type = type; return this; }
        public MemoryQueryBuilder category(String category) { query.category = category; return this; }
        public MemoryQueryBuilder keyword(String keyword) { query.keyword = keyword; return this; }
        public MemoryQueryBuilder topK(int topK) { query.topK = topK; return this; }
        public MemoryQueryBuilder minImportance(double minImportance) { query.minImportance = minImportance; return this; }
        public MemoryQueryBuilder types(List<MemoryType> types) { query.types = types; return this; }
        public MemoryQuery build() { return query; }
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public String getAgentId() { return agentId; }
    public MemoryType getType() { return type; }
    public String getCategory() { return category; }
    public String getKeyword() { return keyword; }
    public int getTopK() { return topK; }
    public double getMinImportance() { return minImportance; }
    public List<MemoryType> getTypes() {
        if (types == null && type != null) {
            types = new ArrayList<>();
            types.add(type);
        }
        return types;
    }
}
