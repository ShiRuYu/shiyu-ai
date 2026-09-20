package com.shiyu.ai.common.vector.api;

import com.shiyu.ai.common.vector.model.VectorStoreOptions;

/**
 * 创建或提供 向量 Store 相关的业务组件和运行时能力。
 */
public interface VectorStoreProvider extends AutoCloseable {

    /**
     * 处理类型。
     *
     * @return 处理结果。
     */
    String type();

    /**
     * 打开向量索引。
     *
     * @param options options 参数。
     *
     * @return 处理结果。
     */
    VectorStore open(VectorStoreOptions options);

    /**
     * 删除向量索引。
     *
     * @param options options 参数。
     */
    default void drop(VectorStoreOptions options) {}

    /**
     * 关闭vectorstore。
     */
    @Override
    default void close() {}
}
