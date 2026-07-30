package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.auth.repository.RoleRepository;
import com.shiyu.ai.dal.auth.repository.TenantRepository;
import com.shiyu.ai.dal.auth.repository.UserRepository;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.repository.UserScopeRoleRepository;
import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.auth.vo.RoleVO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserScopeRoleRepository userWorkspaceRoleRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final MenuService menuService;

    public RoleServiceImpl(RoleRepository roleRepository,
                           UserScopeRoleRepository userWorkspaceRoleRepository,
                           UserRepository userRepository,
                           TenantRepository tenantRepository,
                           MenuService menuService) {
        this.roleRepository = roleRepository;
        this.userWorkspaceRoleRepository = userWorkspaceRoleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.menuService = menuService;
    }

    @Override
    public PageData<RoleVO> getRoleList(Number pageNum, Number pageSize, String name) {
        log.info("获取角色列表，pageNum: {}, pageSize: {}, name: {}", pageNum, pageSize, name);
        
        Pair<Long, List<RoleBO>> result = roleRepository.selectPage(pageNum, pageSize, name);
        List<RoleBO> roleBOs = result.getRight();
        
        // 批量查询菜单权限（修复 N+1 问题）
        List<Long> roleIds = roleBOs.stream().map(RoleBO::getId).toList();
        java.util.Map<Long, List<Long>> roleMenusMap = roleRepository.selectMenuIdsByRoleIds(roleIds);
        for (RoleBO roleBO : roleBOs) {
            roleBO.setPermissions(roleMenusMap.getOrDefault(roleBO.getId(), java.util.Collections.emptyList()));
        }
        
        List<RoleVO> roleVOs = MapstructUtils.convert(roleBOs, RoleVO.class);
        
        return new PageData<>(roleVOs, result.getLeft());
    }

    @Override
    public List<RoleBO> getAllRoles(String status, Long tenantId) {
        log.info("获取所有角色，status: {}, tenantId: {}", status, tenantId);
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (!isAssignableTenantScope(currentTenantId,
                tenantId == null ? currentTenantId : tenantId)) {
            return List.of();
        }
        Long targetTenantId = tenantId == null ? currentTenantId : tenantId;
        return roleRepository.selectAllByTenant(status, targetTenantId).stream()
                .filter(role -> targetTenantId.equals(role.getTenantId()))
                .toList();
    }

    @Override
    public RoleBO getRoleDetail(Long id, Long tenantId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (!isAssignableTenantScope(currentTenantId, tenantId)
                || !roleRepository.isRoleOwnedByTenant(id, tenantId)) {
            return null;
        }
        RoleBO role = roleRepository.selectById(id);
        if (role == null) return null;
        List<Long> menuIds = roleRepository.selectMenuIdsByRoleId(
                id, role.getTenantId(), tenantId);
        role.setPermissions(menuIds);
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Long id, RoleBO roleBO) {
        log.info("修改角色，id: {}", id);
        
        RoleBO existingRole = roleRepository.selectById(id);
        if (existingRole == null) {
            return false;
        }
        Long targetTenantId = resolveAssignableTenantId(roleBO.getTenantId());
        if (targetTenantId == null || !targetTenantId.equals(existingRole.getTenantId())) {
            return false;
        }
        
        // 保存角色基本信息
        roleBO.setId(id);
        roleBO.setTenantId(targetTenantId);
        boolean success = roleRepository.update(roleBO);
        
        // 如果提供了permissions，则更新角色-菜单关联
        if (success && roleBO.getPermissions() != null) {
            if (!areMenusAssignable(roleBO.getPermissions())) {
                throw new IllegalArgumentException("菜单不存在、已停用或超出当前租户可见范围");
            }
            // 先删除旧的关联
            roleRepository.deleteRoleMenus(id, targetTenantId, targetTenantId);
            // 再插入新的关联，授权关系归属于目标角色租户。
            roleRepository.insertRoleMenus(
                    id, targetTenantId, targetTenantId, roleBO.getPermissions());
        }
        
        if (success) menuService.evictAllRouteMenuCache();
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceRoleMenus(Long id, Long tenantId, List<Long> menuIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null
                || !roleRepository.isRoleOwnedByTenant(id, tenantId)) {
            return false;
        }
        if (!isAssignableTenantScope(currentTenantId, tenantId)) {
            return false;
        }
        if (!areMenusAssignable(menuIds)) {
            return false;
        }
        roleRepository.deleteRoleMenus(id, tenantId, tenantId);
        if (menuIds != null && !menuIds.isEmpty()) {
            roleRepository.insertRoleMenus(
                    id, tenantId, tenantId, menuIds.stream().distinct().toList());
        }
        menuService.evictAllRouteMenuCache();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        log.info("删除角色，id: {}", id);

        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !roleRepository.isRoleInScope(id, currentTenantId)) {
            return false;
        }
        boolean success = roleRepository.deleteRoleAndRelations(id);
        if (success) menuService.evictAllRouteMenuCache();
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserRoles(Long roleId, Long tenantId, List<Long> userIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        log.info("取消分配角色，roleId: {}, tenantId: {}, userIds: {}, currentTenantId: {}",
                roleId, tenantId, userIds, currentTenantId);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        if (currentTenantId == null) {
            log.warn("当前没有作用域租户，跳过角色移除");
            return false;
        }
        if (!roleRepository.isRoleOwnedByTenant(roleId, tenantId)) {
            log.warn("角色不属于当前租户作用域或已停用，roleId={}, currentTenantId={}",
                    roleId, currentTenantId);
            return false;
        }
        if (!isAssignableTenantScope(currentTenantId, tenantId)) {
            return false;
        }
        for (Long userId : userIds) {
            if (!userRepository.isUserInScope(userId, currentTenantId)) {
                log.warn("目标用户不属于当前租户作用域，userId={}, currentTenantId={}",
                        userId, currentTenantId);
                return false;
            }
            com.mybatisflex.core.query.QueryWrapper qw = new com.mybatisflex.core.query.QueryWrapper();
            qw.eq(UserScopeRoleDO::getUserId, userId)
               .eq(UserScopeRoleDO::getRoleId, roleId)
               .eq(UserScopeRoleDO::getTenantId, tenantId);
            userWorkspaceRoleRepository.deleteByQuery(qw);
            menuService.evictRouteMenuCache(userId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUserRoles(Long roleId, Long tenantId, List<Long> userIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        log.info("分配角色，roleId: {}, tenantId: {}, userIds: {}, currentTenantId: {}",
                roleId, tenantId, userIds, currentTenantId);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        if (currentTenantId == null) {
            log.warn("当前没有作用域租户，跳过角色分配");
            return false;
        }
        if (!roleRepository.isRoleOwnedByTenant(roleId, tenantId)) {
            log.warn("角色不属于当前租户作用域或已停用，roleId={}, currentTenantId={}",
                    roleId, currentTenantId);
            return false;
        }
        if (!isAssignableTenantScope(currentTenantId, tenantId)) {
            return false;
        }
        for (Long userId : userIds) {
            if (!userRepository.isUserInScope(userId, currentTenantId)) {
                log.warn("目标用户不属于当前租户作用域，userId={}, currentTenantId={}",
                        userId, currentTenantId);
                return false;
            }
            UserScopeRoleDO uwr = new UserScopeRoleDO();
            uwr.setUserId(userId);
            uwr.setTenantId(tenantId);
            uwr.setRoleId(roleId);
            userWorkspaceRoleRepository.insert(uwr);
            menuService.evictRouteMenuCache(userId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(RoleBO roleBO) {
        log.info("新增角色");
        Long targetTenantId = resolveAssignableTenantId(roleBO.getTenantId());
        if (targetTenantId == null) {
            return false;
        }
        roleBO.setTenantId(targetTenantId);
        
        // 保存角色基本信息
        RoleBO savedRole = roleRepository.insert(roleBO);
        
        // 如果提供了permissions，则保存角色-菜单关联
        if (roleBO.getPermissions() != null && !roleBO.getPermissions().isEmpty()) {
            if (!areMenusAssignable(roleBO.getPermissions())) {
                throw new IllegalArgumentException("菜单不存在、已停用或超出当前租户可见范围");
            }
            roleRepository.insertRoleMenus(
                    savedRole.getId(), targetTenantId, targetTenantId, roleBO.getPermissions());
        }
        menuService.evictAllRouteMenuCache();
        return true;
    }

    private boolean areMenusAssignable(List<Long> menuIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null) {
            return false;
        }
        return roleRepository.areMenusInTenantScope(
                menuIds == null ? List.of() : menuIds,
                List.of(currentTenantId));
    }

    private Long resolveAssignableTenantId(Long requestedTenantId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        Long targetTenantId = requestedTenantId == null ? currentTenantId : requestedTenantId;
        return isAssignableTenantScope(currentTenantId, targetTenantId)
                ? targetTenantId : null;
    }

    private boolean isAssignableTenantScope(Long currentTenantId, Long tenantId) {
        if (currentTenantId == null || tenantId == null) {
            return false;
        }
        var tenant = tenantRepository.selectById(tenantId);
        if (tenant == null) {
            return false;
        }
        if (tenant.getStatus() == null || tenant.getStatus() != 1
                || (tenant.getDelFlag() != null && tenant.getDelFlag() != 0)) {
            return false;
        }
        if (currentTenantId.equals(tenantId)) {
            return true;
        }

        // 父租户超级管理员可以查看当前租户的后代租户角色。
        // 普通用户、子租户普通管理员以及兄弟租户均不能跨租户查询。
        if (!LoginContextHolder.isSuperAdmin()
                && !LoginContextHolder.isParentSuperAdminSwitch()) {
            return false;
        }
        return tenantRepository.selectDescendantIds(currentTenantId).contains(tenantId);
    }
}
