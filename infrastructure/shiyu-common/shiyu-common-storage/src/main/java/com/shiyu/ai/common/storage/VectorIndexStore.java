package com.shiyu.ai.common.storage;

import java.util.List;

/** Vector persistence boundary for a future distributed implementation; JVector remains the default. */
public interface VectorIndexStore {
    void upsert(String namespace, String id, float[] vector);
    List<Match> search(String namespace, float[] vector, int limit);
    void delete(String namespace, String id);
    record Match(String id, float score) { }
}
