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

/**
 * 负责 Object 相关数据的存储或后台处理。
 */
public interface ObjectStorage {

    /**
     * 执行 Object 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param originalName 用于完成本次业务处理的 originalName 参数。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param size 每页返回的数据数量。
     * @param inputStream 用于完成本次业务处理的 inputStream 参数。
     * @return 返回 Object 相关操作生成的结果数据。
     */
    StoredObject put(
            String namespace,
            String originalName,
            String contentType,
            long size,
            InputStream inputStream)
            throws IOException;

    /**
     * 创建或保存 Object 相关业务数据，并返回处理结果。
     *
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     * @return 返回 Object 相关操作生成的结果数据。
     */
    ReadableObject open(String objectKey) throws IOException;

    /**
     * 删除或移除 Object 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     */
    void delete(String objectKey) throws IOException;

    /**
     * 封装 Stored Object 相关的不可变数据及其字段约束。
     */
    record StoredObject(
            String objectKey,
            String originalName,
            String contentType,
            long size,
            String provider) {}

    /**
     * 封装 Readable Object 相关的不可变数据及其字段约束。
     */
    record ReadableObject(
            InputStream inputStream, String originalName, String contentType, long size)
            implements AutoCloseable {
        /**
         * {@code close} 释放或移除当前操作涉及的资源。
         */
        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }
}
