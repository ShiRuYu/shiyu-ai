package com.shiyu.ai.common.storage.file.port;

import com.shiyu.ai.common.storage.api.FileStorage;
import com.shiyu.ai.common.storage.api.StoredFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * KeyedFileStorage 接口，定义基础设施模块的能力边界。
 */
public interface KeyedFileStorage extends FileStorage {

    /**
     * 执行 {@code uploadAtKey} 定义的接口操作。
     *
     * @param key 方法参数。
     * @param originalName 方法参数。
     * @param contentType 方法参数。
     * @param size 方法参数。
     * @param inputStream 方法参数。
     *
     * @return 操作结果。
     */
    StoredFile uploadAtKey(
            String key, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException;
}
