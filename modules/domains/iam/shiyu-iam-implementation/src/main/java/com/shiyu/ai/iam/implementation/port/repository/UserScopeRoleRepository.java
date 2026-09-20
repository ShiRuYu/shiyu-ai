package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 用户 Scope 角色 的持久化查询、保存和删除，并维护数据访问边界。
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
     * 查询 用户 Scope 角色 相关业务数据，并返回处理结果。
     *
     * @param userIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    default List<UserScopeRoleBO> selectByUserIds(List<Long> userIds) {
        return userIds == null
                ? List.of()
                : userIds.stream().flatMap(id -> selectByUserId(id).stream()).toList();
    }

    /**
     * 创建或保存 用户 Scope 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userScopeRole 用于完成本次业务处理的 userScopeRole 参数。
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
     * 删除或移除 用户 Scope 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     */
    void deleteByUserIdRoleIdAndTenantId(Long userId, Long roleId, TenantId tenantId);
}
