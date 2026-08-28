package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.vo.RoleVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/** Role application contract. */
public interface RoleService {
    List<RoleVO> allRolesView(ActorContext actor, String status, TenantId tenantId);
    RoleVO detailView(ActorContext actor, Long id, TenantId tenantId);
    boolean createRole(ActorContext actor, RoleRequest request);
    boolean updateRole(ActorContext actor, Long id, RoleRequest request);
    PageData<RoleVO> getRoleList(ActorContext actor, Number pageNum, Number pageSize, String name);
    boolean replaceRoleMenus(ActorContext actor, Long id, TenantId tenantId, List<Long> menuIds);
    boolean deleteRole(ActorContext actor, Long id);
    boolean removeUserRoles(ActorContext actor, Long id, TenantId tenantId, List<Long> userIds);
    boolean assignUserRoles(ActorContext actor, Long id, TenantId tenantId, List<Long> userIds);
}
