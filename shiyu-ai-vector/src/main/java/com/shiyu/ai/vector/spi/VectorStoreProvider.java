package com.shiyu.ai.vector.spi;

import com.shiyu.ai.vector.config.VectorStoreProperties;

/**
 * VectorStore 提供者 SPI — 用于工厂的反射发现
 *
 * <p>每个 VectorStore 实现通过此接口注册自身，由 {@code VectorStoreFactory}
 * 通过 ServiceLoader 或反射加载。新增实现时只需创建对应的 Provider 类，
 * 无需修改工厂代码。</p>
 */
public interface VectorStoreProvider {

    /** 返回向量存储类型标识（如 "inmemory", "jvector", "pgvector"） */
    String type();

    /** 创建对应的 VectorStore 实例 */
    VectorStore create(VectorStoreProperties properties);
}
