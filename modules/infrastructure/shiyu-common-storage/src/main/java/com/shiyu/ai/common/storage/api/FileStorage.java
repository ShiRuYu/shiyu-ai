package com.shiyu.ai.common.storage.api;
import com.shiyu.ai.common.storage.api.*;
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

public interface FileStorage {

    StoredFile upload(String namespace, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException;

    List<StoredFile> list(String namespace) throws IOException;

    StorageObject open(String key) throws IOException;

    void delete(String key) throws IOException;
}
