package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 负责 文件 相关数据的存储或后台处理。
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
     * 查询 文件 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
