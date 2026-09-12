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

/**
 * FileStorage 接口，定义基础设施模块的能力边界。
 */
public interface FileStorage {

    /**
     * 上传文件。
     *
     * @param namespace namespace 参数。
     * @param originalName originalName 参数。
     * @param contentType contentType 参数。
     * @param size size 参数。
     * @param inputStream inputStream 参数。
     *
     * @return 处理结果。
     */
    StoredFile upload(
            String namespace,
            String originalName,
            String contentType,
            long size,
            InputStream inputStream)
            throws IOException;

    /**
     * 查询文件storage列表。
     *
     * @param namespace namespace 参数。
     *
     * @return 结果列表。
     */
    List<StoredFile> list(String namespace) throws IOException;

    /**
     * 打开向量索引。
     *
     * @param key key 参数。
     *
     * @return 处理结果。
     */
    StorageObject open(String key) throws IOException;

    /**
     * 删除文件storage。
     *
     * @param key key 参数。
     */
    void delete(String key) throws IOException;
}
