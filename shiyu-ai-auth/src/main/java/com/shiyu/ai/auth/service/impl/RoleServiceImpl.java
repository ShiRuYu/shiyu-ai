package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.auth.repository.RoleRepository;
import com.shiyu.ai.dal.auth.repository.UserRepository;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.repository.UserScopeRoleRepository;
import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.auth.vo.RolePageResponse;
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
    private final MenuService menuService;

    public RoleServiceImpl(RoleRepository roleRepository,
                           UserScopeRoleRepository userWorkspaceRoleRepository,
                           UserRepository userRepository,
                           MenuService menuService) {
        this.roleRepository = roleRepository;
        this.userWorkspaceRoleRepository = userWorkspaceRoleRepository;
        this.userRepository = userRepository;
        this.menuService = menuService;
    }

    @Override
    public RolePageResponse getRoleList(Number pageNo, Number pageSize, String name) {
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        Pair<Long, List<RoleBO>> result = roleRepository.selectPage(pageNo, pageSize, name);
        List<RoleBO> roleBOs = result.getRight();
        
        // 批量查询菜单权限（修复 N+1 问题）
        List<Long> roleIds = roleBOs.stream().map(RoleBO::getId).toList();
        java.util.Map<Long, List<Long>> roleMenusMap = roleRepository.selectMenuIdsByRoleIds(roleIds);
        for (RoleBO roleBO : roleBOs) {
            roleBO.setPermissions(roleMenusMap.getOrDefault(roleBO.getId(), java.util.Collections.emptyList()));
        }
        
        List<RoleVO> roleVOs = MapstructUtils.convert(roleBOs, RoleVO.class);
        
        RolePageResponse response = new RolePageResponse();
        response.setItems(roleVOs);
        response.setTotal(result.getLeft());
        
        return response;
    }

    @Override
    public List<RoleBO> getAllRoles(String status) {
        log.info("获取所有角色，status: {}", status);
        return roleRepository.selectAll(status);
    }

    @Override
    public RoleBO getRoleDetail(Long id) {
        RoleBO role = roleRepository.selectById(id);
        if (role == null) return null;
        List<Long> menuIds = roleRepository.selectMenuIdsByRoleId(id);
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
        
        // 保存角色基本信息
        roleBO.setId(id);
        boolean success = roleRepository.update(roleBO);
        
        // 如果提供了permissions，则更新角色-菜单关联
        if (success && roleBO.getPermissions() != null) {
            // 先删除旧的关联
            roleRepository.deleteRoleMenus(id);
            // 再插入新的关联
            roleRepository.insertRoleMenus(id, roleBO.getPermissions());
        }
        
        if (success) menuService.evictAllRouteMenuCache();
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        log.info("删除角色，id: {}", id);
        
        // 先删除角色-菜单关联
        roleRepository.deleteRoleMenus(id);
        
        // 再删除角色
        boolean success = roleRepository.deleteById(id);
        if (success) menuService.evictAllRouteMenuCache();
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserRoles(Long roleId, List<Long> userIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        log.info("取消分配角色，roleId: {}, userIds: {}, currentTenantId: {}", roleId, userIds, currentTenantId);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        if (currentTenantId == null) {
            log.warn("当前没有作用域租户，跳过角色移除");
            return false;
        }
        if (!roleRepository.isRoleInScope(roleId, currentTenantId)) {
            log.warn("角色不属于当前租户作用域或已停用，roleId={}, currentTenantId={}",
                    roleId, currentTenantId);
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
               .eq(UserScopeRoleDO::getScopedTenantId, currentTenantId)
               .eq(UserScopeRoleDO::getTenantId, currentTenantId);
            userWorkspaceRoleRepository.deleteByQuery(qw);
            menuService.evictRouteMenuCache(userId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUserRoles(Long roleId, List<Long> userIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        log.info("分配角色，roleId: {}, userIds: {}, currentTenantId: {}", roleId, userIds, currentTenantId);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        if (currentTenantId == null) {
            log.warn("当前没有作用域租户，跳过角色分配");
            return false;
        }
        if (!roleRepository.isRoleInScope(roleId, currentTenantId)) {
            log.warn("角色不属于当前租户作用域或已停用，roleId={}, currentTenantId={}",
                    roleId, currentTenantId);
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
            uwr.setScopedTenantId(currentTenantId);
            uwr.setRoleId(roleId);
            uwr.setTenantId(currentTenantId);
            userWorkspaceRoleRepository.insert(uwr);
            menuService.evictRouteMenuCache(userId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(RoleBO roleBO) {
        log.info("新增角色");
        
        // 保存角色基本信息
        RoleBO savedRole = roleRepository.insert(roleBO);
        
        // 如果提供了permissions，则保存角色-菜单关联
        if (roleBO.getPermissions() != null && !roleBO.getPermissions().isEmpty()) {
            roleRepository.insertRoleMenus(savedRole.getId(), roleBO.getPermissions());
        }
        menuService.evictAllRouteMenuCache();
        return true;
    }
}
