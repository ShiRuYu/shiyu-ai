package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Set;

/**
 * AuthUserLookupRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface AuthUserLookupRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param userId 用户标识。
     *
     * @return 操作结果。
     */
    UserBO selectUserById(Long userId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param userId 用户标识。
     * @param extInfo 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean updateUserExtInfo(Long userId, String extInfo);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userId 用户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<UserScopeRoleBO> selectUserScopeRoles(Long userId);

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
     *
     * @return 操作结果。
     */
    RoleBO selectTenantSuperRole(TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param roleIds 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<RoleBO> selectRolesByIds(Set<Long> roleIds);

    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作结果。
     */
    TenantBO selectTenantById(TenantId tenantId);
}
