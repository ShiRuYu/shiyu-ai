package com.shiyu.ai.common.vector.model;

import java.util.Map;
import java.util.Objects;

/** 向量搜索请求 — Builder 模式 */
public class VectorSearchRequest {

    /**
     * queryVector 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final float[] queryVector;
    /**
     * topK 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int topK;
    /**
     * minScore 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final double minScore;
    /**
     * filter 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Map<String, Object> filter;
    /**
     * searchType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final VectorSearchType searchType;

    private VectorSearchRequest(Builder builder) {
        this.queryVector =
                Objects.requireNonNull(builder.queryVector, "Query vector must not be null");
        if (builder.topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than zero");
        }
        this.topK = builder.topK;
        this.minScore = builder.minScore;
        this.filter = builder.filter == null ? Map.of() : Map.copyOf(builder.filter);
        this.searchType =
                Objects.requireNonNull(builder.searchType, "Search type must not be null");
    }

    /**
     * {@code builder} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@code getQueryVector} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public float[] getQueryVector() {
        return queryVector;
    }

    /**
     * {@code getTopK} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getTopK() {
        return topK;
    }

    /**
     * {@code getMinScore} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public double getMinScore() {
        return minScore;
    }

    /**
     * {@code getFilter} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> getFilter() {
        return filter;
    }

    /**
     * {@code getSearchType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public VectorSearchType getSearchType() {
        return searchType;
    }

    /**
     * {@code Builder} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    public static class Builder {
        /**
         * queryVector 属性，保存当前对象中的业务数据或协作依赖。
         */
        private float[] queryVector;
        /**
         * topK 属性，保存当前对象中的业务数据或协作依赖。
         */
        private int topK = 10;
        /**
         * minScore 属性，保存当前对象中的业务数据或协作依赖。
         */
        private double minScore = 0.0;
        /**
         * filter 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Map<String, Object> filter;
        /**
         * searchType 属性，保存当前对象中的业务数据或协作依赖。
         */
        private VectorSearchType searchType = VectorSearchType.ANN;

        /**
         * {@code queryVector} 查询并返回当前操作所需的数据。
         *
         * @param queryVector 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder queryVector(float[] queryVector) {
            this.queryVector = queryVector;
            return this;
        }

        /**
         * {@code topK} 将当前对象转换为目标表示形式。
         *
         * @param topK 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder topK(int topK) {
            this.topK = topK;
            return this;
        }

        /**
         * {@code minScore} 执行当前类型定义的业务操作。
         *
         * @param minScore 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder minScore(double minScore) {
            this.minScore = minScore;
            return this;
        }

        /**
         * {@code filter} 执行当前类型定义的业务操作。
         *
         * @param filter 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder filter(Map<String, Object> filter) {
            this.filter = filter;
            return this;
        }

        /**
         * {@code searchType} 查询并返回当前操作所需的数据。
         *
         * @param searchType 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder searchType(VectorSearchType searchType) {
            this.searchType = searchType;
            return this;
        }

        /**
         * {@code build} 执行当前类型定义的业务操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public VectorSearchRequest build() {
            return new VectorSearchRequest(this);
        }
    }
}
