package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.vo.RoleVO;
import com.shiyu.ai.common.core.api.PageData;

import java.util.List;

/** Role application contract. */
public interface RoleService {
    List<RoleVO> allRolesView(String status, Long tenantId);
    RoleVO detailView(Long id, Long tenantId);
    boolean createRole(RoleRequest request);
    boolean updateRole(Long id, RoleRequest request);
    PageData<RoleVO> getRoleList(Number pageNum, Number pageSize, String name);
    boolean replaceRoleMenus(Long id, Long tenantId, List<Long> menuIds);
    boolean deleteRole(Long id);
    boolean removeUserRoles(Long id, Long tenantId, List<Long> userIds);
    boolean assignUserRoles(Long id, Long tenantId, List<Long> userIds);
}
