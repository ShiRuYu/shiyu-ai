package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * UserScopeRoleRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface UserScopeRoleRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userId 用户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<UserScopeRoleBO> selectByUserId(Long userId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userIds 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    default List<UserScopeRoleBO> selectByUserIds(List<Long> userIds) {
        return userIds == null
                ? List.of()
                : userIds.stream().flatMap(id -> selectByUserId(id).stream()).toList();
    }

    /**
     * 创建并保存业务对象。
     *
     * @param userScopeRole 方法参数。
     */
    void insert(UserScopeRoleBO userScopeRole);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param userId 用户标识。
     * @param tenantId 租户标识。
     */
    void deleteByUserIdAndTenantId(Long userId, TenantId tenantId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param userId 用户标识。
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     */
    void deleteByUserIdRoleIdAndTenantId(Long userId, Long roleId, TenantId tenantId);
}
