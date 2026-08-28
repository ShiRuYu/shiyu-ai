package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.port.repository.RoleRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.port.repository.UserRepository;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.vo.RoleVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.auth.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
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
    @Override public List<RoleVO> allRolesView(ActorContext actor, String status, TenantId tenantId) { return MapstructUtils.convert(getAllRoles(requireActor(actor), status, tenantId), RoleVO.class); }
    @Override public RoleVO detailView(ActorContext actor, Long id, TenantId tenantId) { return MapstructUtils.convert(getRoleDetail(requireActor(actor), id, tenantId), RoleVO.class); }
    @Override public boolean createRole(ActorContext actor, RoleRequest request) { return createRole(requireActor(actor), MapstructUtils.convert(request, RoleBO.class)); }
    @Override public boolean updateRole(ActorContext actor, Long id, RoleRequest request) { return updateRole(requireActor(actor), id, MapstructUtils.convert(request, RoleBO.class)); }

    private final RoleRepository roleRepository;
    private final UserScopeRoleRepository userScopeRoleRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final MenuService menuService;

    public RoleServiceImpl(RoleRepository roleRepository,
                           UserScopeRoleRepository userScopeRoleRepository,
                           UserRepository userRepository,
                           TenantRepository tenantRepository,
                           MenuService menuService) {
        this.roleRepository = roleRepository;
        this.userScopeRoleRepository = userScopeRoleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.menuService = menuService;
    }

    @Override
    public PageData<RoleVO> getRoleList(ActorContext actor, Number pageNum, Number pageSize, String name) {
        requireActor(actor);
        log.info("获取角色列表，pageNum: {}, pageSize: {}, name: {}", pageNum, pageSize, name);
        
        Pair<Long, List<RoleBO>> result = roleRepository.selectPage(actor.tenantId(), pageNum, pageSize, name);
        List<RoleBO> roleBOs = result.getRight();
        
        // 批量查询菜单权限（修复 N+1 问题）
        List<Long> roleIds = roleBOs.stream().map(RoleBO::getId).toList();
        java.util.Map<Long, List<Long>> roleMenusMap = roleRepository.selectMenuIdsByRoleIds(actor.tenantId(), roleIds);
        for (RoleBO roleBO : roleBOs) {
            roleBO.setPermissions(roleMenusMap.getOrDefault(roleBO.getId(), java.util.Collections.emptyList()));
        }
        
        List<RoleVO> roleVOs = MapstructUtils.convert(roleBOs, RoleVO.class);
        
        return new PageData<>(roleVOs, result.getLeft());
    }

    private List<RoleBO> getAllRoles(ActorContext actor, String status, TenantId tenantId) {
        log.info("获取所有角色，status: {}, tenantId: {}", status, tenantId);
        if (tenantId == null) {
            log.warn("拒绝缺少租户的角色查询");
            return List.of();
        }
        if (!isAssignableTenantScope(actor, tenantId)) {
            return List.of();
        }
        return roleRepository.selectAllByTenant(status, tenantId).stream()
                .filter(role -> role != null && role.getTenantId() != null
                        && tenantId.value() == role.getTenantId())
                .toList();
    }

    private RoleBO getRoleDetail(ActorContext actor, Long id, TenantId tenantId) {
        if (tenantId == null || !isAssignableTenantScope(actor, tenantId)
                || !roleRepository.isRoleOwnedByTenant(id, tenantId)) {
            return null;
        }
        RoleBO role = roleRepository.selectById(id, tenantId);
        if (role == null) return null;
        List<Long> menuIds = roleRepository.selectMenuIdsByRoleId(
                id, new TenantId(role.getTenantId()), tenantId);
        role.setPermissions(menuIds);
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    private boolean updateRole(ActorContext actor, Long id, RoleBO roleBO) {
        log.info("修改角色，id: {}", id);
        if (roleBO == null || roleBO.getTenantId() == null) {
            log.warn("拒绝缺少租户的角色更新，id={}", id);
            return false;
        }
        
        RoleBO existingRole = roleRepository.selectById(id, actor.tenantId());
        if (existingRole == null) {
            return false;
        }
        Long targetTenantId = resolveAssignableTenantId(actor, roleBO.getTenantId());
        if (targetTenantId == null || !targetTenantId.equals(existingRole.getTenantId())) {
            return false;
        }
        
        // 保存角色基本信息
        roleBO.setId(id);
        roleBO.setTenantId(targetTenantId);
        boolean success = roleRepository.update(roleBO);
        
        // 如果提供了permissions，则更新角色-菜单关联
        if (success && roleBO.getPermissions() != null) {
            if (!areMenusAssignable(actor, roleBO.getPermissions())) {
                throw new IllegalArgumentException("菜单不存在、已停用或超出当前租户可见范围");
            }
            // 先删除旧的关联
            roleRepository.deleteRoleMenus(id, new TenantId(targetTenantId), new TenantId(targetTenantId));
            // 再插入新的关联，授权关系归属于目标角色租户。
            roleRepository.insertRoleMenus(
                    id, new TenantId(targetTenantId), new TenantId(targetTenantId), roleBO.getPermissions());
        }
        
        if (success) menuService.evictAllRouteMenuCache();
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceRoleMenus(ActorContext actor, Long id, TenantId tenantId, List<Long> menuIds) {
        requireActor(actor);
        if (tenantId == null || !roleRepository.isRoleOwnedByTenant(id, tenantId)) {
            return false;
        }
        if (!isAssignableTenantScope(actor, tenantId)) {
            return false;
        }
        if (!areMenusAssignable(actor, menuIds)) {
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
    public boolean deleteRole(ActorContext actor, Long id) {
        log.info("删除角色，id: {}", id);

        requireActor(actor);
        Long currentTenantId = actor.tenantId().value();
        if (currentTenantId == null || !roleRepository.isRoleInScope(id, actor.tenantId())) {
            return false;
        }
        boolean success = roleRepository.deleteRoleAndRelations(id, actor.tenantId());
        if (success) menuService.evictAllRouteMenuCache();
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserRoles(ActorContext actor, Long roleId, TenantId tenantId, List<Long> userIds) {
        requireActor(actor);
        log.info("取消分配角色，roleId: {}, tenantId: {}, userIds: {}, currentTenantId: {}",
                roleId, tenantId, userIds, actor.tenantId());
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        if (tenantId == null) {
            log.warn("当前没有作用域租户，跳过角色移除");
            return false;
        }
        if (!roleRepository.isRoleOwnedByTenant(roleId, tenantId)) {
            log.warn("角色不属于当前租户作用域或已停用，roleId={}, currentTenantId={}",
                    roleId, actor.tenantId());
            return false;
        }
        if (!isAssignableTenantScope(actor, tenantId)) {
            return false;
        }
        for (Long userId : userIds) {
            if (!userRepository.isUserInScope(userId, actor.tenantId())) {
                log.warn("目标用户不属于当前租户作用域，userId={}, currentTenantId={}",
                        userId, actor.tenantId());
                return false;
            }
            userScopeRoleRepository.deleteByUserIdRoleIdAndTenantId(
                    userId, roleId, tenantId);
            menuService.evictRouteMenuCache(userId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUserRoles(ActorContext actor, Long roleId, TenantId tenantId, List<Long> userIds) {
        requireActor(actor);
        log.info("分配角色，roleId: {}, tenantId: {}, userIds: {}, currentTenantId: {}",
                roleId, tenantId, userIds, actor.tenantId());
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        if (tenantId == null) {
            log.warn("当前没有作用域租户，跳过角色分配");
            return false;
        }
        if (!roleRepository.isRoleOwnedByTenant(roleId, tenantId)) {
            log.warn("角色不属于当前租户作用域或已停用，roleId={}, currentTenantId={}",
                    roleId, actor.tenantId());
            return false;
        }
        if (!isAssignableTenantScope(actor, tenantId)) {
            return false;
        }
        for (Long userId : userIds) {
            if (!userRepository.isUserInScope(userId, actor.tenantId())) {
                log.warn("目标用户不属于当前租户作用域，userId={}, currentTenantId={}",
                        userId, actor.tenantId());
                return false;
            }
            UserScopeRoleBO uwr = new UserScopeRoleBO();
            uwr.setUserId(userId);
            uwr.setTenantId(tenantId.value());
            uwr.setRoleId(roleId);
            userScopeRoleRepository.insert(uwr);
            menuService.evictRouteMenuCache(userId);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    private boolean createRole(ActorContext actor, RoleBO roleBO) {
        log.info("新增角色");
        if (roleBO == null || roleBO.getTenantId() == null) {
            log.warn("拒绝缺少租户的角色创建");
            return false;
        }
        Long targetTenantId = resolveAssignableTenantId(actor, roleBO.getTenantId());
        if (targetTenantId == null) {
            return false;
        }
        roleBO.setTenantId(targetTenantId);
        
        // 保存角色基本信息
        RoleBO savedRole = roleRepository.insert(roleBO);
        
        // 如果提供了permissions，则保存角色-菜单关联
        if (roleBO.getPermissions() != null && !roleBO.getPermissions().isEmpty()) {
            if (!areMenusAssignable(actor, roleBO.getPermissions())) {
                throw new IllegalArgumentException("菜单不存在、已停用或超出当前租户可见范围");
            }
            roleRepository.insertRoleMenus(
                    savedRole.getId(), new TenantId(targetTenantId), new TenantId(targetTenantId), roleBO.getPermissions());
        }
        menuService.evictAllRouteMenuCache();
        return true;
    }

    private boolean areMenusAssignable(ActorContext actor, List<Long> menuIds) {
        Long currentTenantId = requireActor(actor).tenantId().value();
        return roleRepository.areMenusInTenantScope(
                menuIds == null ? List.of() : menuIds,
                List.of(currentTenantId));
    }

    private Long resolveAssignableTenantId(ActorContext actor, Long requestedTenantId) {
        if (requestedTenantId == null) {
            return null;
        }
        TenantId targetTenantId;
        try {
            targetTenantId = new TenantId(requestedTenantId);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return isAssignableTenantScope(actor, targetTenantId)
                ? targetTenantId.value() : null;
    }

    private boolean isAssignableTenantScope(ActorContext actor, TenantId tenantId) {
        if (tenantId == null) {
            return false;
        }
        TenantId currentTenantId = requireActor(actor).tenantId();
        var tenant = tenantRepository.selectById(tenantId.value());
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
        if (!actor.platformAdmin() && !actor.parentSuperAdminSwitch()) {
            return false;
        }
        return tenantRepository.selectDescendantIds(currentTenantId).contains(tenantId.value());
    }

    private ActorContext requireActor(ActorContext actor) {
        if (actor == null) {
            throw new IllegalArgumentException("actor context is required");
        }
        return actor;
    }
}
