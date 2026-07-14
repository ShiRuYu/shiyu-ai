package com.shiyu.ai.vector.spi.impl;

import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.spi.VectorStore;
import com.shiyu.ai.vector.spi.VectorStoreProvider;

/**
 * JVectorStore 提供者
 */
public class JVectorStoreProvider implements VectorStoreProvider {

    @Override
    public String type() {
        return "jvector";
    }

    @Override
    public VectorStore create(VectorStoreProperties properties) {
        return new JVectorStore(properties);
    }
}
