package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * TenantRoleRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface TenantRoleRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作结果。
     */
    TenantBO selectTenantById(TenantId tenantId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param roleId 方法参数。
     *
     * @return 操作结果。
     */
    RoleBO selectRoleById(Long roleId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param roleCode 方法参数。
     *
     * @return 操作结果。
     */
    RoleBO selectEnabledRoleByCode(TenantId tenantId, String roleCode);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作结果。
     */
    RoleBO selectTenantSuperRole(TenantId tenantId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作结果。
     */
    String selectTenantNameById(TenantId tenantId);
}
