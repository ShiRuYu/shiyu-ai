package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAiAppRepository implements AiAppRepository {
    private final Map<String, AiApp> apps = new ConcurrentHashMap<>();
    private final Map<String, AiAppVersion> versions = new ConcurrentHashMap<>();
    @Override public void insert(AiApp app) { if (apps.putIfAbsent(app.id(), app) != null) throw new IllegalStateException("app already exists"); }
    @Override public Optional<AiApp> find(String id, TenantId tenantId, long ownerUserId) { return Optional.ofNullable(apps.get(id)).filter(a -> a.tenantId().equals(requireTenant(tenantId)) && a.ownerUserId().value() == ownerUserId); }
    @Override public Optional<AiApp> findByTenant(String id, TenantId tenantId) { return Optional.ofNullable(apps.get(id)).filter(a -> a.tenantId().equals(requireTenant(tenantId))); }
    @Override public List<AiApp> list(TenantId tenantId, long ownerUserId, int limit) { return apps.values().stream().filter(a -> a.tenantId().equals(requireTenant(tenantId)) && a.ownerUserId().value() == ownerUserId).sorted(Comparator.comparing(AiApp::updatedAt).reversed()).limit(Math.max(1, Math.min(limit, 100))).toList(); }
    @Override public void insertVersion(AiAppVersion version) { if (versions.putIfAbsent(version.id(), version) != null) throw new IllegalStateException("app version already exists"); }
    @Override public Optional<AiAppVersion> findVersion(String appId, String versionId, TenantId tenantId) { return Optional.ofNullable(versions.get(versionId)).filter(v -> v.tenantId().equals(requireTenant(tenantId)) && v.appId().equals(appId)); }
    @Override public List<AiAppVersion> versions(String appId, TenantId tenantId) { return versions.values().stream().filter(v -> v.tenantId().equals(requireTenant(tenantId)) && v.appId().equals(appId)).sorted(Comparator.comparing(AiAppVersion::createdAt).reversed()).toList(); }
    @Override public int publishVersion(String appId, String versionId, TenantId tenantId) {
        AiAppVersion current = findVersion(appId, versionId, tenantId).orElseThrow(() -> new IllegalArgumentException("app version not found"));
        if ("ARCHIVED".equals(current.status())) throw new IllegalStateException("archived version cannot publish");
        if ("PUBLISHED".equals(current.status())) return 0;
        versions.put(versionId, new AiAppVersion(current.id(), current.appId(), current.tenantId(), current.version(), current.configJson(), "PUBLISHED", current.createdAt(), Instant.now()));
        apps.computeIfPresent(appId, (id, app) -> new AiApp(app.id(), app.tenantId(), app.ownerUserId(), app.name(), app.description(), app.status(), versionId, app.createdAt(), Instant.now()));
        return 1;
    }
    @Override public int archiveVersion(String appId, String versionId, TenantId tenantId) {
        AiAppVersion current = findVersion(appId, versionId, tenantId).orElseThrow(() -> new IllegalArgumentException("app version not found"));
        versions.put(versionId, new AiAppVersion(current.id(), current.appId(), current.tenantId(), current.version(), current.configJson(), "ARCHIVED", current.createdAt(), current.publishedAt()));
        return 1;
    }

    private static TenantId requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId;
    }
}
