package com.shiyu.ai.common.storage.file;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

/**
 * Domain callback used by the generic storage upload pipeline. The storage
 * module owns bytes and upload sessions; a domain module owns authorization
 * and metadata registration.
 */
public interface ResumableUploadHandler {

    void authorize(UploadActor actor, Long spaceId);

    String namespace(TenantId tenantId, Long spaceId);

    RegistrationResult register(UploadActor actor, UploadRegistration request);

    record UploadActor(TenantId tenantId, UserId userId, RoleId roleId, boolean platformAdmin) {
        public UploadActor {
            if (tenantId == null || userId == null) {
                throw new IllegalArgumentException("tenantId and userId are required");
            }
        }
    }

    record UploadRegistration(TenantId tenantId, Long spaceId, String title,
                               String originalName, String objectKey,
                               String storageProvider, String contentType,
                               long size, String checksum) {
    }

    record RegistrationResult(Object value, boolean duplicate) {
    }
}
