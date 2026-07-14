package com.shiyu.ai.vector.spi.impl;

import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.spi.VectorStore;
import com.shiyu.ai.vector.spi.VectorStoreProvider;

/**
 * InMemoryVectorStore 提供者
 */
public class InMemoryVectorStoreProvider implements VectorStoreProvider {

    @Override
    public String type() {
        return "inmemory";
    }

    @Override
    public VectorStore create(VectorStoreProperties properties) {
        return new InMemoryVectorStore();
    }
}
