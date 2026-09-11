package com.shiyu.ai.iam.implementation.application.identity;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.service.MenuService;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;
import com.shiyu.ai.iam.implementation.vo.TenantInfoVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Role and tenant identity switching use cases. */
@Slf4j
public final class AuthIdentityUseCase {
    private final UserRepository userRepository;
    private final UserScopeRoleRepository userScopeRoleRepository;
    private final TenantRoleRepository tenantRoleRepository;
    private final TenantRepository tenantRepository;
    private final MenuService menuService;
    private final AuthTenantContextSupport contextSupport;

    public AuthIdentityUseCase(
            UserRepository userRepository,
            UserScopeRoleRepository userScopeRoleRepository,
            TenantRoleRepository tenantRoleRepository,
            TenantRepository tenantRepository,
            MenuService menuService,
            AuthTenantContextSupport contextSupport) {
        this.userRepository = userRepository;
        this.userScopeRoleRepository = userScopeRoleRepository;
        this.tenantRoleRepository = tenantRoleRepository;
        this.tenantRepository = tenantRepository;
        this.menuService = menuService;
        this.contextSupport = contextSupport;
    }

    public boolean switchCurrentRole(Long userId, Long roleId) {
        log.info("切换角色, userIdPresent={}, roleIdPresent={}", userId != null, roleId != null);
        try {
            if (userId == null || roleId == null) {
                return false;
            }
            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                log.warn("用户不存在, userIdPresent={}", userId != null);
                return false;
            }
            List<UserScopeRoleBO> assignments = userScopeRoleRepository.selectByUserId(userId);
            Long currentTenantId =
                    contextSupport.resolveCurrentTenantId(user.getExtInfo(), assignments);
            if (currentTenantId == null
                    || assignments == null
                    || assignments.stream()
                            .noneMatch(
                                    item ->
                                            currentTenantId.equals(item.getTenantId())
                                                    && roleId.equals(item.getRoleId())
                                                    && contextSupport.isActiveAssignment(item))) {
                log.warn(
                        "角色不属于当前租户作用域, userIdPresent={}, roleIdPresent={}, tenantSelected={}",
                        userId != null,
                        roleId != null,
                        currentTenantId != null);
                return false;
            }
            List<RoleBO> roles = userRepository.selectRolesByUserId(userId);
            RoleBO target =
                    roles == null
                            ? null
                            : roles.stream()
                                    .filter(role -> roleId.equals(role.getId()))
                                    .findFirst()
                                    .orElse(null);
            if (target == null) {
                log.warn("角色不存在, userIdPresent={}", userId != null);
                return false;
            }
            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("roleId", target.getId());
            roleMap.put("roleName", target.getName());
            roleMap.put("roleKey", target.getCode());
            Map<String, Object> extInfoMap = contextSupport.parseExtInfo(user.getExtInfo());
            extInfoMap.put("currentRole", roleMap);
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            if (!userRepository.update(user)) {
                log.warn(
                        "切换角色失败，租户上下文持久化被拒绝, userIdPresent={}, roleIdPresent={}",
                        userId != null,
                        roleId != null);
                return false;
            }
            log.info(
                    "切换角色成功, userIdPresent={}, roleNamePresent={}",
                    userId != null,
                    target.getName() != null);
            SaTokenHelper.clearUserContextSession();
            menuService.evictRouteMenuCache(userId);
            return true;
        } catch (Exception e) {
            log.error(
                    "切换角色异常, userIdPresent={}, roleIdPresent={}, errorType={},"
                            + " errorMessageLength={}",
                    userId != null,
                    roleId != null,
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            return false;
        }
    }

    public boolean switchCurrentTenant(Long userId, TenantId tenantId) {
        log.info("切换租户, userIdPresent={}, tenantPresent={}", userId != null, tenantId != null);
        try {
            if (tenantId == null) {
                log.warn("切换租户失败，tenantId 为空, userIdPresent={}", userId != null);
                return false;
            }
            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                return false;
            }
            List<UserScopeRoleBO> assignments = userScopeRoleRepository.selectByUserId(userId);
            Map<String, Object> extInfoMap = contextSupport.parseExtInfo(user.getExtInfo());
            Long homeTenantId = contextSupport.numberValue(extInfoMap.get("homeTenantId"));
            if (homeTenantId == null && assignments != null) {
                homeTenantId =
                        assignments.stream()
                                .filter(contextSupport::isActiveAssignment)
                                .map(UserScopeRoleBO::getTenantId)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null);
            }
            if (homeTenantId == null) {
                return false;
            }
            Long currentTenantId = contextSupport.numberValue(extInfoMap.get("currentTenantId"));
            if (currentTenantId == null) {
                currentTenantId = homeTenantId;
            }
            long targetTenantValue = tenantId.value();
            TenantBO targetTenant = tenantRoleRepository.selectTenantById(tenantId);
            if (!contextSupport.isActiveTenant(targetTenant)) {
                return false;
            }

            List<RoleBO> assignedRoles =
                    assignments == null
                            ? List.of()
                            : assignments.stream()
                                    .filter(
                                            item ->
                                                    Long.valueOf(targetTenantValue)
                                                                    .equals(item.getTenantId())
                                                            && contextSupport.isActiveAssignment(
                                                                    item))
                                    .map(UserScopeRoleBO::getRoleId)
                                    .filter(Objects::nonNull)
                                    .map(tenantRoleRepository::selectRoleById)
                                    .filter(
                                            role ->
                                                    role != null
                                                            && Long.valueOf(targetTenantValue)
                                                                    .equals(role.getTenantId())
                                                            && role.getStatus() != null
                                                            && role.getStatus() == 1
                                                            && (role.getDelFlag() == null
                                                                    || role.getDelFlag() == 0))
                                    .sorted(Comparator.comparing(RoleBO::getId))
                                    .toList();
            Long preferredRoleId =
                    extInfoMap.get("currentRole") instanceof Map<?, ?> roleMap
                            ? contextSupport.numberValue(roleMap.get("roleId"))
                            : null;
            RoleBO assignedRole =
                    preferredRoleId == null
                            ? null
                            : assignedRoles.stream()
                                    .filter(role -> preferredRoleId.equals(role.getId()))
                                    .findFirst()
                                    .orElse(null);
            if (assignedRole == null && !assignedRoles.isEmpty()) {
                assignedRole = assignedRoles.get(0);
            }
            final Long homeTenant = homeTenantId;
            boolean homeTenantSuper =
                    assignments != null
                            && assignments.stream()
                                    .filter(
                                            item ->
                                                    homeTenant.equals(item.getTenantId())
                                                            && contextSupport.isActiveAssignment(
                                                                    item))
                                    .map(UserScopeRoleBO::getRoleId)
                                    .map(tenantRoleRepository::selectRoleById)
                                    .anyMatch(contextSupport::isTenantSuperRole);
            boolean returningHome = homeTenantId.equals(targetTenantValue);
            boolean switchedAwayFromHome = !homeTenantId.equals(currentTenantId);
            Long allowedRootTenantId =
                    homeTenantSuper && switchedAwayFromHome ? currentTenantId : homeTenantId;
            boolean targetInAllowedSubtree =
                    tenantRepository
                            .selectDescendantIds(new TenantId(allowedRootTenantId))
                            .contains(targetTenantValue);
            if (homeTenantSuper
                    && switchedAwayFromHome
                    && !returningHome
                    && !targetInAllowedSubtree) {
                return false;
            }
            if (assignedRole == null
                    && !(homeTenantSuper && (returningHome || targetInAllowedSubtree))) {
                log.warn(
                        "切换租户失败，缺少租户归属或父租户超级管理员权限, userIdPresent={}, tenantPresent={}",
                        userId != null,
                        tenantId != null);
                return false;
            }

            boolean switchingChild = !returningHome;
            boolean parentSuperAdminSwitch =
                    switchingChild
                            && assignedRole == null
                            && homeTenantSuper
                            && targetInAllowedSubtree;
            extInfoMap.put("currentTenantId", targetTenantValue);
            extInfoMap.put("homeTenantId", homeTenantId);
            RoleBO role =
                    parentSuperAdminSwitch
                            ? contextSupport.findTenantSuperRole(targetTenantValue)
                            : assignedRole;
            if (role != null) {
                Map<String, Object> roleMap = new LinkedHashMap<>();
                roleMap.put("roleId", role.getId());
                roleMap.put("roleName", role.getName());
                roleMap.put("roleKey", role.getCode());
                extInfoMap.put("currentRole", roleMap);
            }
            extInfoMap.put("switchMode", parentSuperAdminSwitch ? "PARENT_SUPER_ADMIN" : "NORMAL");
            if (parentSuperAdminSwitch) {
                extInfoMap.put("switchFromTenantId", homeTenantId);
            } else {
                extInfoMap.remove("switchFromTenantId");
            }
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            if (!userRepository.update(user)) {
                log.warn(
                        "切换租户失败，租户上下文持久化被拒绝, userIdPresent={}, tenantPresent={}",
                        userId != null,
                        tenantId != null);
                return false;
            }
            log.info(
                    "切换租户成功, userIdPresent={}, tenantPresent={}", userId != null, tenantId != null);
            SaTokenHelper.clearUserContextSession();
            menuService.evictRouteMenuCache(userId);
            return true;
        } catch (Exception e) {
            log.error(
                    "切换租户异常, userIdPresent={}, tenantPresent={}, errorType={},"
                            + " errorMessageLength={}",
                    userId != null,
                    tenantId != null,
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            return false;
        }
    }

    public List<TenantInfoVO> getUserTenants(ActorContext actor, Long userId) {
        Objects.requireNonNull(actor, "actor must not be null");
        List<UserScopeRoleBO> assignments = userScopeRoleRepository.selectByUserId(userId);
        List<TenantInfoVO> tenants = contextSupport.buildTenantList(assignments);
        UserBO user = userRepository.selectById(userId);
        if (user == null) {
            return tenants;
        }
        Map<String, Object> extInfo = contextSupport.parseExtInfo(user.getExtInfo());
        Long homeTenantId = contextSupport.numberValue(extInfo.get("homeTenantId"));
        Long currentTenantId = contextSupport.numberValue(extInfo.get("currentTenantId"));
        if (currentTenantId == null && actor.tenantId() != null) {
            currentTenantId = actor.tenantId().value();
        }
        if (homeTenantId == null && actor.homeTenantId() != null) {
            homeTenantId = actor.homeTenantId().value();
        }
        final Long resolvedHomeTenantId = homeTenantId;
        boolean homeTenantSuperAdmin =
                resolvedHomeTenantId != null
                        && assignments != null
                        && assignments.stream()
                                .filter(
                                        item ->
                                                resolvedHomeTenantId.equals(item.getTenantId())
                                                        && contextSupport.isActiveAssignment(item))
                                .map(UserScopeRoleBO::getRoleId)
                                .map(tenantRoleRepository::selectRoleById)
                                .anyMatch(contextSupport::isTenantSuperRole);
        if (homeTenantSuperAdmin
                && resolvedHomeTenantId != null
                && currentTenantId != null
                && !resolvedHomeTenantId.equals(currentTenantId)) {
            return contextSupport.buildScopedTenantList(currentTenantId, resolvedHomeTenantId);
        }
        if (homeTenantSuperAdmin) {
            return contextSupport.buildScopedTenantList(resolvedHomeTenantId, resolvedHomeTenantId);
        }
        return tenants;
    }

    private static int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}
