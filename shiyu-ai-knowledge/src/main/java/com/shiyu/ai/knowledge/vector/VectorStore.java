package com.shiyu.ai.knowledge.vector;

import java.util.List;

public interface VectorStore {

    void upsert(VectorRecord record);

    default void upsertBatch(List<VectorRecord> records) {
        for (VectorRecord r : records) {
            upsert(r);
        }
    }

    List<VectorRecord> search(float[] queryVector, int topK);

    void delete(String id);

    default void rebuild() {
    }

    default int size() {
        return 0;
    }
}
