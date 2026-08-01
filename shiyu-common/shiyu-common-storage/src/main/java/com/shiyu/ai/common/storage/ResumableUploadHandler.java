package com.shiyu.ai.common.storage;

/**
 * Domain callback used by the generic storage upload pipeline. The storage
 * module owns bytes and upload sessions; a domain module owns authorization
 * and metadata registration.
 */
public interface ResumableUploadHandler {

    void authorize(Long tenantId, Long spaceId);

    String namespace(Long tenantId, Long spaceId);

    RegistrationResult register(UploadRegistration request);

    record UploadRegistration(Long tenantId, Long spaceId, String title,
                               String originalName, String objectKey,
                               String storageProvider, String contentType,
                               long size, String checksum) {
    }

    record RegistrationResult(Object value, boolean duplicate) {
    }
}
