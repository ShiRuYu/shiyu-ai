package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public interface RoleRepository {
    Pair<Long, List<RoleBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name);
    List<RoleBO> selectAll(TenantId tenantId, String status);
    List<RoleBO> selectAllByTenant(String status, TenantId tenantId);
    RoleBO selectById(Long id, TenantId tenantId);
    RoleBO insert(RoleBO roleBO);
    boolean update(RoleBO roleBO);
    boolean deleteRoleAndRelations(Long roleId, TenantId tenantId);
    boolean isRoleInScope(Long roleId, TenantId currentTenantId);
    boolean isRoleOwnedByTenant(Long roleId, TenantId tenantId);
    boolean areMenusInTenantScope(List<Long> menuIds, List<Long> allowedTenantIds);
    Map<Long, List<Long>> selectMenuIdsByRoleIds(TenantId tenantId, List<Long> roleIds);
    List<Long> selectMenuIdsByRoleId(Long roleId, TenantId roleTenantId, TenantId tenantId);
    void insertRoleMenus(Long roleId, TenantId roleTenantId, TenantId tenantId, List<Long> menuIds);
    void deleteRoleMenus(Long roleId, TenantId roleTenantId, TenantId tenantId);
}
