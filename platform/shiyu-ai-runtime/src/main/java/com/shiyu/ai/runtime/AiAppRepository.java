package com.shiyu.ai.runtime;

import java.util.List;
import java.util.Optional;

public interface AiAppRepository {
    void insert(AiApp app);
    Optional<AiApp> find(String id, long tenantId, long ownerUserId);
    Optional<AiApp> findByTenant(String id, long tenantId);
    List<AiApp> list(long tenantId, long ownerUserId, int limit);
    void insertVersion(AiAppVersion version);
    Optional<AiAppVersion> findVersion(String appId, String versionId, long tenantId);
    List<AiAppVersion> versions(String appId, long tenantId);
    int publishVersion(String appId, String versionId, long tenantId);
    int archiveVersion(String appId, String versionId, long tenantId);
}
