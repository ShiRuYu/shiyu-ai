package com.shiyu.ai.common.storage.file;

import com.shiyu.ai.common.storage.api.FileStorage;
import com.shiyu.ai.common.storage.api.StoredFile;

import java.io.IOException;
import java.io.InputStream;

/** Internal extension used by the migration command to preserve object keys. */
interface KeyedFileStorage extends FileStorage {

    StoredFile uploadAtKey(
            String key, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException;
}
