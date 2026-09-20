package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * 负责 租户 角色 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface TenantRoleRepository {
    /**
     * 查询 租户 角色 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 租户 角色 相关操作生成的结果数据。
     */
    TenantBO selectTenantById(TenantId tenantId);

    /**
     * 查询 租户 角色 相关业务数据，并返回处理结果。
     *
     * @param roleId 用于定位role的标识。
     * @return 返回 租户 角色 相关操作生成的结果数据。
     */
    RoleBO selectRoleById(Long roleId);

    /**
     * 查询 租户 角色 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param roleCode 用于完成本次业务处理的 roleCode 参数。
     * @return 返回 租户 角色 相关操作生成的结果数据。
     */
    RoleBO selectEnabledRoleByCode(TenantId tenantId, String roleCode);

    /**
     * 查询 租户 角色 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 租户 角色 相关操作生成的结果数据。
     */
    RoleBO selectTenantSuperRole(TenantId tenantId);

    /**
     * 查询 租户 角色 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 租户 角色 相关操作生成的结果数据。
     */
    String selectTenantNameById(TenantId tenantId);
}
