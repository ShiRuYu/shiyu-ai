package com.shiyu.ai.common.vector.model;

/**
 * 封装 向量 Store Options 相关的不可变数据及其字段约束。
 */
public record VectorStoreOptions(String namespace, int dimension, String dataDir) {

    public VectorStoreOptions {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("Vector store namespace must not be blank");
        }
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be greater than zero");
        }
    }

    /**
     * 执行 向量 Store Options 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param dimension 用于完成本次业务处理的 dimension 参数。
     * @param dataDir 用于完成本次业务处理的 dataDir 参数。
     * @return 返回 向量 Store Options 相关操作生成的结果数据。
     */
    public static VectorStoreOptions of(String namespace, int dimension, String dataDir) {
        return new VectorStoreOptions(namespace, dimension, dataDir);
    }
}
