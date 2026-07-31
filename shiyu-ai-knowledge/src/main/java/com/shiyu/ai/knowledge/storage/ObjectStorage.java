package com.shiyu.ai.knowledge.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Knowledge-engine object storage boundary. Business services must not depend on
 * a concrete local or remote storage implementation.
 */
public interface ObjectStorage {

    StoredObject put(String namespace, String originalName, String contentType,
                     long size, InputStream inputStream) throws IOException;

    ReadableObject open(String objectKey) throws IOException;

    void delete(String objectKey) throws IOException;

    record StoredObject(String objectKey, String originalName, String contentType,
                        long size, String provider) {
    }

    record ReadableObject(InputStream inputStream, String originalName,
                          String contentType, long size) implements AutoCloseable {
        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }
}
