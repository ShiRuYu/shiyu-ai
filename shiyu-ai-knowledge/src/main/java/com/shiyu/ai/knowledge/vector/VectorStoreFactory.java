package com.shiyu.ai.knowledge.vector;

import com.shiyu.ai.knowledge.vector.impl.InMemoryVectorStore;
import com.shiyu.ai.knowledge.vector.config.VectorStoreProperties;

public class VectorStoreFactory {

    public static VectorStore create(String type, VectorStoreProperties properties) {
        return switch (type.toLowerCase()) {
            case "inmemory" -> new InMemoryVectorStore();
            case "hnsw" -> createHnsw(properties);
            case "qdrant" -> createQdrant(properties);
            default -> throw new IllegalArgumentException("Unknown VectorStore type: " + type);
        };
    }

    private static VectorStore createHnsw(VectorStoreProperties properties) {
        try {
            Class<?> clazz = Class.forName("com.shiyu.ai.knowledge.vector.impl.HnswVectorStore");
            return (VectorStore) clazz.getDeclaredConstructor(VectorStoreProperties.class)
                    .newInstance(properties);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                    "HNSWVectorStore 不可用，请添加 usearch 依赖", e);
        }
    }

    private static VectorStore createQdrant(VectorStoreProperties properties) {
        try {
            Class<?> clazz = Class.forName("com.shiyu.ai.knowledge.vector.impl.QdrantVectorStore");
            return (VectorStore) clazz.getDeclaredConstructor(VectorStoreProperties.class)
                    .newInstance(properties);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                    "QdrantVectorStore 不可用，请添加 qdrant-client 依赖", e);
        }
    }
}
