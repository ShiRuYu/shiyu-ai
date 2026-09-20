package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.List;

/**
 * 负责 认证 的持久化查询、保存和删除，并维护数据访问边界。
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
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param roleCode 用于完成本次业务处理的 roleCode 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<String> selectCodesByUserIdAndRoleCode(UserId userId, TenantId tenantId, String roleCode);

    /**
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param roleCode 用于完成本次业务处理的 roleCode 参数。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<String> selectCodesByRoleCodeAndTenant(String roleCode, TenantId tenantId);

    /**
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<String> selectCodesByUsername(String username, TenantId tenantId);

    /**
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param currentTenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<String> selectCodesByUserId(UserId userId, TenantId currentTenantId);

    /**
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<String> selectCodesByRoleId(Long roleId, TenantId tenantId);
}
