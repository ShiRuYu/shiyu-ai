package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public interface RoleRepository {
    Pair<Long, List<RoleBO>> selectPage(Number pageNo, Number pageSize, String name);
    List<RoleBO> selectAll(String status);
    List<RoleBO> selectAllByTenant(String status, Long tenantId);
    RoleBO selectById(Long id);
    RoleBO insert(RoleBO roleBO);
    boolean update(RoleBO roleBO);
    boolean deleteById(Long id);
    boolean deleteRoleAndRelations(Long roleId);
    boolean isRoleInScope(Long roleId, Long currentTenantId);
    boolean isRoleOwnedByTenant(Long roleId, Long tenantId);
    boolean areMenusInTenantScope(List<Long> menuIds, List<Long> allowedTenantIds);
    List<MenuBO> selectMenusByRoleId(Long roleId);
    List<Long> selectMenuIdsByRoleId(Long roleId);
    Map<Long, List<Long>> selectMenuIdsByRoleIds(List<Long> roleIds);
    void insertRoleMenus(Long roleId, List<Long> menuIds);
    List<Long> selectMenuIdsByRoleId(Long roleId, Long roleTenantId, Long tenantId);
    void insertRoleMenus(Long roleId, Long roleTenantId, Long tenantId, List<Long> menuIds);
    void deleteRoleMenus(Long roleId);
    void deleteRoleMenus(Long roleId, Long roleTenantId, Long tenantId);
}
