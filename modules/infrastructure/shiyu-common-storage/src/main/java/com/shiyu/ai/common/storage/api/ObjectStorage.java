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

/**
 * ObjectStorage 接口，定义基础设施模块的能力边界。
 */
public interface ObjectStorage {

    /**
     * 执行 {@code put} 定义的接口操作。
     *
     * @param namespace 方法参数。
     * @param originalName 方法参数。
     * @param contentType 方法参数。
     * @param size 方法参数。
     * @param inputStream 方法参数。
     *
     * @return 操作结果。
     */
    StoredObject put(
            String namespace,
            String originalName,
            String contentType,
            long size,
            InputStream inputStream)
            throws IOException;

    /**
     * 执行 {@code open} 定义的接口操作。
     *
     * @param objectKey 方法参数。
     *
     * @return 操作结果。
     */
    ReadableObject open(String objectKey) throws IOException;

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param objectKey 方法参数。
     */
    void delete(String objectKey) throws IOException;

    /**
     * {@code StoredObject} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     * @param originalName originalName 属性，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
     * @param provider 提供方，表示该记录组件承载的数据。
     */
    record StoredObject(
            String objectKey,
            String originalName,
            String contentType,
            long size,
            String provider) {}

    /**
     * {@code ReadableObject} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param inputStream inputStream 属性，表示该记录组件承载的数据。
     * @param originalName originalName 属性，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
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
