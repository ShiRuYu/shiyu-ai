package com.shiyu.ai.common.vector.model;

import java.util.Map;
import java.util.Objects;

/**
 * 封装 向量 Search 操作所需的请求条件和输入数据。
 */
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
     * 构建或转换 向量 Search 相关业务数据，并返回处理结果。
     *
     * @return 返回 向量 Search 相关操作生成的结果数据。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 查询 向量 Search 相关业务数据，并返回处理结果。
     *
     * @return 返回 向量 Search 相关操作生成的结果数据。
     */
    public float[] getQueryVector() {
        return queryVector;
    }

    /**
     * 查询 向量 Search 相关业务数据，并返回处理结果。
     *
     * @return 返回 向量 Search 相关操作生成的结果数据。
     */
    public int getTopK() {
        return topK;
    }

    /**
     * 查询 向量 Search 相关业务数据，并返回处理结果。
     *
     * @return 返回 向量 Search 相关操作生成的结果数据。
     */
    public double getMinScore() {
        return minScore;
    }

    /**
     * 查询 向量 Search 相关业务数据，并返回处理结果。
     *
     * @return 返回 向量 Search 相关操作生成的结果数据。
     */
    public Map<String, Object> getFilter() {
        return filter;
    }

    /**
     * 查询 向量 Search 相关业务数据，并返回处理结果。
     *
     * @return 返回 向量 Search 相关操作生成的结果数据。
     */
    public VectorSearchType getSearchType() {
        return searchType;
    }

    /**
     * 构建 Builder 相关的对象、流程或运行时配置。
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
         * 查询 Builder 相关业务数据，并返回处理结果。
         *
         * @param queryVector 用于完成本次业务处理的 queryVector 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder queryVector(float[] queryVector) {
            this.queryVector = queryVector;
            return this;
        }

        /**
         * 构建或转换 Builder 相关业务数据，并返回处理结果。
         *
         * @param topK 用于完成本次业务处理的 topK 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder topK(int topK) {
            this.topK = topK;
            return this;
        }

        /**
         * 执行 Builder 相关业务数据，并返回处理结果。
         *
         * @param minScore 用于完成本次业务处理的 minScore 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder minScore(double minScore) {
            this.minScore = minScore;
            return this;
        }

        /**
         * 执行 Builder 相关业务数据，并返回处理结果。
         *
         * @param filter 用于筛选目标数据的查询条件。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder filter(Map<String, Object> filter) {
            this.filter = filter;
            return this;
        }

        /**
         * 查询 Builder 相关业务数据，并返回处理结果。
         *
         * @param searchType 用于完成本次业务处理的 searchType 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder searchType(VectorSearchType searchType) {
            this.searchType = searchType;
            return this;
        }

        /**
         * 构建或转换 Builder 相关业务数据，并返回处理结果。
         *
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public VectorSearchRequest build() {
            return new VectorSearchRequest(this);
        }
    }
}
