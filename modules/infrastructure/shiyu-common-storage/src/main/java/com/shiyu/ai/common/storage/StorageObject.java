package com.shiyu.ai.common.storage;

import java.io.InputStream;

public record StorageObject(
        InputStream inputStream,
        String name,
        String contentType,
        long size
) {
}
