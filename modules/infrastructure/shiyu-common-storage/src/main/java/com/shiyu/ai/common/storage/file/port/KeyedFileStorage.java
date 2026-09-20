package com.shiyu.ai.common.storage.file.port;

import com.shiyu.ai.common.storage.api.FileStorage;
import com.shiyu.ai.common.storage.api.StoredFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 负责 Keyed 文件 相关数据的存储或后台处理。
 */
public interface KeyedFileStorage extends FileStorage {

    /**
     * 执行 Keyed 文件 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param originalName 用于完成本次业务处理的 originalName 参数。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param size 每页返回的数据数量。
     * @param inputStream 用于完成本次业务处理的 inputStream 参数。
     * @return 返回 Keyed 文件 相关操作生成的结果数据。
     */
    StoredFile uploadAtKey(
            String key, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException;
}
