package com.shiyu.ai.common.vector.model;

/**
 * 承载向量存储的命名空间、维度和数据目录配置。
 * @param namespace 命名空间，表示该记录组件承载的数据。
 * @param dimension 向量维度，表示该记录组件承载的数据。
 * @param dataDir 数据目录，表示该记录组件承载的数据。
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
     * {@code of} 执行当前类型定义的业务操作。
     *
     * @param namespace 参数值，用于执行当前操作。
     * @param dimension 参数值，用于执行当前操作。
     * @param dataDir 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static VectorStoreOptions of(String namespace, int dimension, String dataDir) {
        return new VectorStoreOptions(namespace, dimension, dataDir);
    }
}
