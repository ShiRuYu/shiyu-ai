package com.shiyu.ai.vector;

/**
 * Generic options used to open an isolated vector store.
 *
 * @param namespace logical isolation key, for example knowledge/tenant/space/version
 * @param dimension embedding vector dimension
 * @param dataDir optional embedded storage directory; remote providers may ignore it
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

    public static VectorStoreOptions of(String namespace, int dimension, String dataDir) {
        return new VectorStoreOptions(namespace, dimension, dataDir);
    }
}
