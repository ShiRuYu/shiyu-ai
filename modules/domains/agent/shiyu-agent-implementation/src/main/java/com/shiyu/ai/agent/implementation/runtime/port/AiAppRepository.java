package com.shiyu.ai.agent.implementation.runtime.port;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * AiAppRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface AiAppRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param app 方法参数。
     */
    void insert(AiApp app);

    /**
     * 根据标识查询对应的数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<AiApp> find(String id, TenantId tenantId, long ownerUserId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<AiApp> findByTenant(String id, TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AiApp> list(TenantId tenantId, long ownerUserId, int limit);

    /**
     * 创建并保存业务对象。
     *
     * @param version 方法参数。
     */
    void insertVersion(AiAppVersion version);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param appId 方法参数。
     * @param versionId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<AiAppVersion> findVersion(String appId, String versionId, TenantId tenantId);

    /**
     * 执行 {@code versions} 定义的接口操作。
     *
     * @param appId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<AiAppVersion> versions(String appId, TenantId tenantId);

    /**
     * 发布或发送业务事件。
     *
     * @param appId 方法参数。
     * @param versionId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    int publishVersion(String appId, String versionId, TenantId tenantId);

    /**
     * 执行 {@code archiveVersion} 定义的接口操作。
     *
     * @param appId 方法参数。
     * @param versionId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    int archiveVersion(String appId, String versionId, TenantId tenantId);
}
