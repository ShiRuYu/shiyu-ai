package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * 负责 角色 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface RoleRepository {
    /**
     * 查询 角色 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
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
     * 查询 角色 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 角色 相关操作生成的结果数据。
     */
    RoleBO selectById(Long id, TenantId tenantId);

    /**
     * 创建或保存 角色 相关业务数据，并返回处理结果。
     *
     * @param roleBO 用于完成本次业务处理的 roleBO 参数。
     * @return 返回 角色 相关操作生成的结果数据。
     */
    RoleBO insert(RoleBO roleBO);

    /**
     * 更新或设置 角色 相关业务数据，并返回处理结果。
     *
     * @param roleBO 用于完成本次业务处理的 roleBO 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean update(RoleBO roleBO);

    /**
     * 删除或移除 角色 相关业务数据，并返回处理结果。
     *
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean deleteRoleAndRelations(Long roleId, TenantId tenantId);

    /**
     * 校验或判断 角色 相关业务数据，并返回处理结果。
     *
     * @param roleId 用于定位role的标识。
     * @param currentTenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean isRoleInScope(Long roleId, TenantId currentTenantId);

    /**
     * 校验或判断 角色 相关业务数据，并返回处理结果。
     *
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean isRoleOwnedByTenant(Long roleId, TenantId tenantId);

    /**
     * 执行 角色 相关业务数据，并返回处理结果。
     *
     * @param menuIds 待处理的业务对象标识集合。
     * @param allowedTenantIds 待处理的业务对象标识集合。
     * @return 返回本次条件判断是否成立。
     */
    boolean areMenusInTenantScope(List<Long> menuIds, List<Long> allowedTenantIds);

    /**
     * 查询 角色 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param roleIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    Map<Long, List<Long>> selectMenuIdsByRoleIds(TenantId tenantId, List<Long> roleIds);

    /**
     * 查询 角色 相关业务数据，并返回处理结果。
     *
     * @param roleId 用于定位role的标识。
     * @param roleTenantId 当前操作涉及的租户标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> selectMenuIdsByRoleId(Long roleId, TenantId roleTenantId, TenantId tenantId);

    /**
     * 创建或保存 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param roleId 用于定位role的标识。
     * @param roleTenantId 当前操作涉及的租户标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param menuIds 待处理的业务对象标识集合。
     */
    void insertRoleMenus(Long roleId, TenantId roleTenantId, TenantId tenantId, List<Long> menuIds);

    /**
     * 删除或移除 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param roleId 用于定位role的标识。
     * @param roleTenantId 当前操作涉及的租户标识。
     * @param tenantId 当前操作涉及的租户标识。
     */
    void deleteRoleMenus(Long roleId, TenantId roleTenantId, TenantId tenantId);
}
