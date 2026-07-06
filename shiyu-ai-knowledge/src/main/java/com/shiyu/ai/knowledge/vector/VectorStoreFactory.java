package com.shiyu.ai.knowledge.vector;

import com.shiyu.ai.knowledge.vector.impl.HnswVectorStore;
import com.shiyu.ai.knowledge.vector.impl.InMemoryVectorStore;
import com.shiyu.ai.knowledge.vector.config.VectorStoreProperties;

public class VectorStoreFactory {

    public static VectorStore create(String type, VectorStoreProperties properties) {
        return switch (type.toLowerCase()) {
            case "inmemory" -> new InMemoryVectorStore();
            case "hnsw" -> createHnsw(properties);
            default -> throw new IllegalArgumentException("Unknown VectorStore type: " + type);
        };
    }

    private static VectorStore createHnsw(VectorStoreProperties properties) {
        return new HnswVectorStore(properties);
    }

}
