package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.iam.implementation.request.RoleRequest;
import com.shiyu.ai.iam.implementation.vo.RoleVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * RoleService 服务接口，负责执行身份与访问领域相关业务操作。
 */
public interface RoleService {
    /**
     * 执行 {@code allRolesView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param status 对象状态。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<RoleVO> allRolesView(ActorContext actor, String status, TenantId tenantId);

    /**
     * 执行 {@code detailView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     *
     * @return 操作结果。
     */
    RoleVO detailView(ActorContext actor, Long id, TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean createRole(ActorContext actor, RoleRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean updateRole(ActorContext actor, Long id, RoleRequest request);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     *
     * @return 操作结果。
     */
    PageData<RoleVO> getRoleList(ActorContext actor, Number pageNum, Number pageSize, String name);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param menuIds 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean replaceRoleMenus(ActorContext actor, Long id, TenantId tenantId, List<Long> menuIds);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteRole(ActorContext actor, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param userIds 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean removeUserRoles(ActorContext actor, Long id, TenantId tenantId, List<Long> userIds);

    /**
     * 执行 {@code assignUserRoles} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param userIds 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean assignUserRoles(ActorContext actor, Long id, TenantId tenantId, List<Long> userIds);
}
