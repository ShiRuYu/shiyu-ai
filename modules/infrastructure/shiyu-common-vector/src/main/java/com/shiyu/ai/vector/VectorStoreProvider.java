package com.shiyu.ai.vector;

/**
 * Public provider boundary for opening global or scoped vector stores.
 * Business modules must depend on this interface instead of concrete backends.
 */
public interface VectorStoreProvider extends AutoCloseable {

    /** Active backend type, such as jvector or inmemory. */
    String type();

    /** Open a vector store isolated by the supplied namespace. */
    VectorStore open(VectorStoreOptions options);

    /** Drop provider-owned state for an obsolete namespace. */
    default void drop(VectorStoreOptions options) {
    }

    /** Release provider-level resources. */
    @Override
    default void close() {
    }
}
