package com.shiyu.ai.agent.implementation.runtime.port;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * 负责 AI 应用 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AiAppRepository {
    /**
     * 创建或保存 AI 应用 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param app 用于完成本次业务处理的 app 参数。
     */
    void insert(AiApp app);

    /**
     * 查询 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AiApp> list(TenantId tenantId, long ownerUserId, int limit);

    /**
     * 创建或保存 AI 应用 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param version 用于完成本次业务处理的 version 参数。
     */
    void insertVersion(AiAppVersion version);

    /**
     * 查询 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param versionId 用于定位version的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<AiAppVersion> findVersion(String appId, String versionId, TenantId tenantId);

    /**
     * 执行 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AiAppVersion> versions(String appId, TenantId tenantId);

    /**
     * 发布或发送 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param versionId 用于定位version的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 AI 应用 相关操作生成的结果数据。
     */
    int publishVersion(String appId, String versionId, TenantId tenantId);

    /**
     * 执行 AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param versionId 用于定位version的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 AI 应用 相关操作生成的结果数据。
     */
    int archiveVersion(String appId, String versionId, TenantId tenantId);
}
