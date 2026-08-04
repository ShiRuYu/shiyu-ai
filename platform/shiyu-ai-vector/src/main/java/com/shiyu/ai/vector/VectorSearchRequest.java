package com.shiyu.ai.vector;

import java.util.Map;
import java.util.Objects;

/**
 * 向量搜索请求 — Builder 模式
 */
public class VectorSearchRequest {

    private final float[] queryVector;
    private final int topK;
    private final double minScore;
    private final Map<String, Object> filter;
    private final VectorSearchType searchType;

    private VectorSearchRequest(Builder builder) {
        this.queryVector = Objects.requireNonNull(builder.queryVector, "Query vector must not be null");
        if (builder.topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than zero");
        }
        this.topK = builder.topK;
        this.minScore = builder.minScore;
        this.filter = builder.filter == null ? Map.of() : Map.copyOf(builder.filter);
        this.searchType = Objects.requireNonNull(builder.searchType, "Search type must not be null");
    }

    public static Builder builder() { return new Builder(); }

    public float[] getQueryVector() { return queryVector; }
    public int getTopK() { return topK; }
    public double getMinScore() { return minScore; }
    public Map<String, Object> getFilter() { return filter; }
    public VectorSearchType getSearchType() { return searchType; }

    public static class Builder {
        private float[] queryVector;
        private int topK = 10;
        private double minScore = 0.0;
        private Map<String, Object> filter;
        private VectorSearchType searchType = VectorSearchType.ANN;

        public Builder queryVector(float[] queryVector) { this.queryVector = queryVector; return this; }
        public Builder topK(int topK) { this.topK = topK; return this; }
        public Builder minScore(double minScore) { this.minScore = minScore; return this; }
        public Builder filter(Map<String, Object> filter) { this.filter = filter; return this; }
        public Builder searchType(VectorSearchType searchType) { this.searchType = searchType; return this; }
        public VectorSearchRequest build() { return new VectorSearchRequest(this); }
    }
}
