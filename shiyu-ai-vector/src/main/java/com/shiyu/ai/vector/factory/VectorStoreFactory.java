package com.shiyu.ai.vector.factory;

import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.impl.jvector.JVectorStore;
import com.shiyu.ai.vector.impl.memory.InMemoryVectorStore;
import com.shiyu.ai.vector.spi.VectorStore;

/**
 * VectorStore 工厂
 */
public class VectorStoreFactory {

    public static VectorStore create(String type, VectorStoreProperties properties) {
        return switch (type.toLowerCase()) {
            case "inmemory" -> new InMemoryVectorStore();
            case "jvector" -> new JVectorStore(properties);
            default -> throw new IllegalArgumentException("Unknown VectorStore type: " + type);
        };
    }
}
