package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** File-oriented storage SPI shared by local and remote storage providers. */
public interface FileStorage {

    /** Stores a file under a namespace and returns its provider-neutral metadata. */
    StoredFile upload(
            String namespace,
            String originalName,
            String contentType,
            long size,
            InputStream inputStream)
            throws IOException;

    /** Lists files visible under a namespace. */
    List<StoredFile> list(String namespace) throws IOException;

    /** Opens a stored file for streaming reads. */
    StorageObject open(String key) throws IOException;

    /** Deletes a stored file by its provider-neutral key. */
    void delete(String key) throws IOException;
}
