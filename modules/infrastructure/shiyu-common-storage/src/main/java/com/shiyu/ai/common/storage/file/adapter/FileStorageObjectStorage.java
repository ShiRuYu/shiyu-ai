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
 * 负责 文件 Storage Object 相关数据的存储或后台处理。
 */
@Component
@RequiredArgsConstructor
public class FileStorageObjectStorage implements ObjectStorage {

    /**
     * storageManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final FileStorageManager storageManager;

    /**
     * 执行 文件 Storage Object 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param originalName 用于完成本次业务处理的 originalName 参数。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param size 每页返回的数据数量。
     * @param inputStream 用于完成本次业务处理的 inputStream 参数。
     * @return 返回 文件 Storage Object 相关操作生成的结果数据。
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
     * 创建或保存 文件 Storage Object 相关业务数据，并返回处理结果。
     *
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     * @return 返回 文件 Storage Object 相关操作生成的结果数据。
     */
    @Override
    public ReadableObject open(String objectKey) throws IOException {
        StorageObject object = storageManager.open(objectKey);
        return new ReadableObject(
                object.inputStream(), object.name(), object.contentType(), object.size());
    }

    /**
     * 删除或移除 文件 Storage Object 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     */
    @Override
    public void delete(String objectKey) throws IOException {
        storageManager.delete(objectKey);
    }
}
