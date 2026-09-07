package com.shiyu.ai.common.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class FileStorageObjectStorage implements ObjectStorage {

    private final FileStorageManager storageManager;

    @Override
    public StoredObject put(String namespace, String originalName, String contentType,
                            long size, InputStream inputStream) throws IOException {
        StoredFile file = storageManager.upload(namespace, originalName, contentType, size, inputStream);
        return new StoredObject(file.key(), file.name(), file.contentType(), file.size(), file.storageType());
    }

    @Override
    public ReadableObject open(String objectKey) throws IOException {
        StorageObject object = storageManager.open(objectKey);
        return new ReadableObject(object.inputStream(), object.name(), object.contentType(), object.size());
    }

    @Override
    public void delete(String objectKey) throws IOException {
        storageManager.delete(objectKey);
    }
}
