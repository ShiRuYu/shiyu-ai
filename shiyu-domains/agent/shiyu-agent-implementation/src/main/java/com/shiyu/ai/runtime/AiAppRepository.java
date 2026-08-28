package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

public interface AiAppRepository {
    void insert(AiApp app);
    Optional<AiApp> find(String id, TenantId tenantId, long ownerUserId);
    Optional<AiApp> findByTenant(String id, TenantId tenantId);
    List<AiApp> list(TenantId tenantId, long ownerUserId, int limit);
    void insertVersion(AiAppVersion version);
    Optional<AiAppVersion> findVersion(String appId, String versionId, TenantId tenantId);
    List<AiAppVersion> versions(String appId, TenantId tenantId);
    int publishVersion(String appId, String versionId, TenantId tenantId);
    int archiveVersion(String appId, String versionId, TenantId tenantId);
}
