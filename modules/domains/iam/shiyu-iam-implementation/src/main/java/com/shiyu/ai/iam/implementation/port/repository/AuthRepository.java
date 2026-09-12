package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.List;

/**
 * AuthRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface AuthRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userId 用户标识。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<String> selectRoleCodesByUserId(UserId userId, TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userId 用户标识。
     * @param tenantId 租户标识。
     * @param roleCode 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<String> selectCodesByUserIdAndRoleCode(UserId userId, TenantId tenantId, String roleCode);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param roleCode 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<String> selectCodesByRoleCodeAndTenant(String roleCode, TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param username 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<String> selectCodesByUsername(String username, TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userId 用户标识。
     * @param currentTenantId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<String> selectCodesByUserId(UserId userId, TenantId currentTenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<String> selectCodesByRoleId(Long roleId, TenantId tenantId);
}
