package com.shiyu.ai.agent.implementation.runtime.adapter.inmemory;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;
import com.shiyu.ai.agent.implementation.runtime.port.AiAppRepository;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code InMemoryAiAppRepository} 定义智能体模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
public class InMemoryAiAppRepository implements AiAppRepository {
    private final Map<String, AiApp> apps = new ConcurrentHashMap<>();
    private final Map<String, AiAppVersion> versions = new ConcurrentHashMap<>();

    /**
     * {@code insert} 执行当前类型定义的业务操作。
     *
     * @param app 参数值，用于执行当前操作。
     */
    @Override
    public void insert(AiApp app) {
        if (apps.putIfAbsent(app.id(), app) != null)
            throw new IllegalStateException("app already exists");
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiApp> find(String id, TenantId tenantId, long ownerUserId) {
        return Optional.ofNullable(apps.get(id))
                .filter(
                        a ->
                                a.tenantId().equals(requireTenant(tenantId))
                                        && a.ownerUserId().value() == ownerUserId);
    }

    /**
     * {@code findByTenant} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiApp> findByTenant(String id, TenantId tenantId) {
        return Optional.ofNullable(apps.get(id))
                .filter(a -> a.tenantId().equals(requireTenant(tenantId)));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AiApp> list(TenantId tenantId, long ownerUserId, int limit) {
        return apps.values().stream()
                .filter(
                        a ->
                                a.tenantId().equals(requireTenant(tenantId))
                                        && a.ownerUserId().value() == ownerUserId)
                .sorted(Comparator.comparing(AiApp::updatedAt).reversed().thenComparing(AiApp::id))
                .limit(Math.max(1, Math.min(limit, 100)))
                .toList();
    }

    /**
     * {@code insertVersion} 执行当前类型定义的业务操作。
     *
     * @param version 参数值，用于执行当前操作。
     */
    @Override
    public void insertVersion(AiAppVersion version) {
        if (versions.putIfAbsent(version.id(), version) != null)
            throw new IllegalStateException("app version already exists");
    }

    /**
     * {@code findVersion} 查询并返回当前操作所需的数据。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiAppVersion> findVersion(String appId, String versionId, TenantId tenantId) {
        return Optional.ofNullable(versions.get(versionId))
                .filter(
                        v ->
                                v.tenantId().equals(requireTenant(tenantId))
                                        && v.appId().equals(appId));
    }

    /**
     * {@code versions} 执行当前类型定义的业务操作。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AiAppVersion> versions(String appId, TenantId tenantId) {
        return versions.values().stream()
                .filter(
                        v ->
                                v.tenantId().equals(requireTenant(tenantId))
                                        && v.appId().equals(appId))
                .sorted(
                        Comparator.comparing(AiAppVersion::createdAt)
                                .reversed()
                                .thenComparing(AiAppVersion::id))
                .toList();
    }

    /**
     * {@code publishVersion} 执行当前模块定义的业务流程。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public synchronized int publishVersion(String appId, String versionId, TenantId tenantId) {
        AiAppVersion current =
                findVersion(appId, versionId, tenantId)
                        .orElseThrow(() -> new IllegalArgumentException("app version not found"));
        if ("ARCHIVED".equals(current.status()))
            throw new IllegalStateException("archived version cannot publish");
        if ("PUBLISHED".equals(current.status())) return 0;
        Instant publishedAt = Instant.now();
        versions.replaceAll(
                (id, version) ->
                        version.appId().equals(appId)
                                        && version.tenantId().equals(requireTenant(tenantId))
                                        && "PUBLISHED".equals(version.status())
                                ? new AiAppVersion(
                                        version.id(),
                                        version.appId(),
                                        version.tenantId(),
                                        version.version(),
                                        version.configJson(),
                                        "ARCHIVED",
                                        version.createdAt(),
                                        version.publishedAt())
                                : version);
        versions.put(
                versionId,
                new AiAppVersion(
                        current.id(),
                        current.appId(),
                        current.tenantId(),
                        current.version(),
                        current.configJson(),
                        "PUBLISHED",
                        current.createdAt(),
                        publishedAt));
        apps.computeIfPresent(
                appId,
                (id, app) ->
                        new AiApp(
                                app.id(),
                                app.tenantId(),
                                app.ownerUserId(),
                                app.name(),
                                app.description(),
                                app.status(),
                                versionId,
                                app.createdAt(),
                                publishedAt));
        return 1;
    }

    /**
     * {@code archiveVersion} 执行当前类型定义的业务操作。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public synchronized int archiveVersion(String appId, String versionId, TenantId tenantId) {
        AiAppVersion current =
                findVersion(appId, versionId, tenantId)
                        .orElseThrow(() -> new IllegalArgumentException("app version not found"));
        if (!"DRAFT".equals(current.status())) return 0;
        versions.put(
                versionId,
                new AiAppVersion(
                        current.id(),
                        current.appId(),
                        current.tenantId(),
                        current.version(),
                        current.configJson(),
                        "ARCHIVED",
                        current.createdAt(),
                        current.publishedAt()));
        return 1;
    }

    private static TenantId requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId;
    }
}
