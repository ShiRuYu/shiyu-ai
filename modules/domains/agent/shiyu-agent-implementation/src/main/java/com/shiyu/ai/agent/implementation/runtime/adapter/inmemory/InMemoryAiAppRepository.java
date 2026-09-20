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
 * 负责 In 记忆 AI 应用 的持久化查询、保存和删除，并维护数据访问边界。
 */
public class InMemoryAiAppRepository implements AiAppRepository {
    private final Map<String, AiApp> apps = new ConcurrentHashMap<>();
    private final Map<String, AiAppVersion> versions = new ConcurrentHashMap<>();

    /**
     * 创建或保存 In 记忆 AI 应用 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param app 用于完成本次业务处理的 app 参数。
     */
    @Override
    public void insert(AiApp app) {
        if (apps.putIfAbsent(app.id(), app) != null)
            throw new IllegalStateException("app already exists");
    }

    /**
     * 查询 In 记忆 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 In 记忆 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<AiApp> findByTenant(String id, TenantId tenantId) {
        return Optional.ofNullable(apps.get(id))
                .filter(a -> a.tenantId().equals(requireTenant(tenantId)));
    }

    /**
     * 查询 In 记忆 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 创建或保存 In 记忆 AI 应用 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param version 用于完成本次业务处理的 version 参数。
     */
    @Override
    public void insertVersion(AiAppVersion version) {
        if (versions.putIfAbsent(version.id(), version) != null)
            throw new IllegalStateException("app version already exists");
    }

    /**
     * 查询 In 记忆 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param versionId 用于定位version的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 执行 In 记忆 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 发布或发送 In 记忆 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param versionId 用于定位version的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 In 记忆 AI 应用 相关操作生成的结果数据。
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
     * 执行 In 记忆 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param versionId 用于定位version的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 In 记忆 AI 应用 相关操作生成的结果数据。
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
