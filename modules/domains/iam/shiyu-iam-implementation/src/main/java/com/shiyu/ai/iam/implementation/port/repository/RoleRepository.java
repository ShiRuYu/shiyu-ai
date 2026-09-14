package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * RoleRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface RoleRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<RoleBO>> selectPage(
            TenantId tenantId, Number pageNo, Number pageSize, String name);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param status 对象状态。
     *
     * @return 符合条件的结果集合。
     */
    List<RoleBO> selectAll(TenantId tenantId, String status);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param status 对象状态。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<RoleBO> selectAllByTenant(String status, TenantId tenantId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     *
     * @return 操作结果。
     */
    RoleBO selectById(Long id, TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param roleBO 方法参数。
     *
     * @return 操作结果。
     */
    RoleBO insert(RoleBO roleBO);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param roleBO 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean update(RoleBO roleBO);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteRoleAndRelations(Long roleId, TenantId tenantId);

    /**
     * 判断当前条件是否满足。
     *
     * @param roleId 方法参数。
     * @param currentTenantId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean isRoleInScope(Long roleId, TenantId currentTenantId);

    /**
     * 判断当前条件是否满足。
     *
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 条件是否满足。
     */
    boolean isRoleOwnedByTenant(Long roleId, TenantId tenantId);

    /**
     * 执行 {@code areMenusInTenantScope} 定义的接口操作。
     *
     * @param menuIds 方法参数。
     * @param allowedTenantIds 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean areMenusInTenantScope(List<Long> menuIds, List<Long> allowedTenantIds);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param roleIds 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    Map<Long, List<Long>> selectMenuIdsByRoleIds(TenantId tenantId, List<Long> roleIds);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param roleId 方法参数。
     * @param roleTenantId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> selectMenuIdsByRoleId(Long roleId, TenantId roleTenantId, TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param roleId 方法参数。
     * @param roleTenantId 方法参数。
     * @param tenantId 租户标识。
     * @param menuIds 方法参数。
     */
    void insertRoleMenus(Long roleId, TenantId roleTenantId, TenantId tenantId, List<Long> menuIds);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param roleId 方法参数。
     * @param roleTenantId 方法参数。
     * @param tenantId 租户标识。
     */
    void deleteRoleMenus(Long roleId, TenantId roleTenantId, TenantId tenantId);
}
