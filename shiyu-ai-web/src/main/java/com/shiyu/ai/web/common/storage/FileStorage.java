package com.shiyu.ai.web.common.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileStorage {

    StoredFile upload(String namespace, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException;

    List<StoredFile> list(String namespace) throws IOException;

    StorageObject open(String key) throws IOException;

    void delete(String key) throws IOException;
}
