package com.shiyu.ai.common.storage.file.adapter;
import com.shiyu.ai.common.storage.file.service.FileStorageManager;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@code FileStorageObjectStorage} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
@RequiredArgsConstructor
public class FileStorageObjectStorage implements ObjectStorage {

    /**
     * storageManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final FileStorageManager storageManager;

    /**
     * {@code put} 执行当前类型定义的业务操作。
     *
     * @param namespace 参数值，用于执行当前操作。
     * @param originalName 参数值，用于执行当前操作。
     * @param contentType 参数值，用于执行当前操作。
     * @param size 参数值，用于执行当前操作。
     * @param inputStream 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public StoredObject put(
            String namespace,
            String originalName,
            String contentType,
            long size,
            InputStream inputStream)
            throws IOException {
        StoredFile file =
                storageManager.upload(namespace, originalName, contentType, size, inputStream);
        return new StoredObject(
                file.key(), file.name(), file.contentType(), file.size(), file.storageType());
    }

    /**
     * {@code open} 执行当前类型定义的业务操作。
     *
     * @param objectKey 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ReadableObject open(String objectKey) throws IOException {
        StorageObject object = storageManager.open(objectKey);
        return new ReadableObject(
                object.inputStream(), object.name(), object.contentType(), object.size());
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param objectKey 参数值，用于执行当前操作。
     */
    @Override
    public void delete(String objectKey) throws IOException {
        storageManager.delete(objectKey);
    }
}
