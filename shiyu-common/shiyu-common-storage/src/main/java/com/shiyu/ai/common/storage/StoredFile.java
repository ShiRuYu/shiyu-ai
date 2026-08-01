package com.shiyu.ai.common.storage;

import java.time.Instant;

public record StoredFile(
        String key,
        String name,
        long size,
        String contentType,
        Instant lastModified,
        String url,
        String storageType
) {
}
