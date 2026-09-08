package com.shiyu.ai.common.storage.vector;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.util.List;

/** Vector persistence boundary for a future distributed implementation; JVector remains the default. */
public interface VectorIndexStore {
    void upsert(String namespace, String id, float[] vector);
    List<Match> search(String namespace, float[] vector, int limit);
    void delete(String namespace, String id);
    record Match(String id, float score) { }
}
