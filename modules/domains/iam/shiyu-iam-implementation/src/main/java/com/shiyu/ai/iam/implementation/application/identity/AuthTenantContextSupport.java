package com.shiyu.ai.iam.implementation.application.identity;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.vo.TenantContextVO;
import com.shiyu.ai.iam.implementation.vo.TenantInfoVO;
import com.shiyu.ai.kernel.context.TenantId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared, side-effect-free tenant and role context rules used by IAM use cases.
 * Repository calls remain explicit so callers never fall back to thread context.
 */
public final class AuthTenantContextSupport {
    private final TenantRepository tenantRepository;
    private final TenantRoleRepository tenantRoleRepository;

    public AuthTenantContextSupport(TenantRepository tenantRepository,
                                    TenantRoleRepository tenantRoleRepository) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "tenantRepository");
        this.tenantRoleRepository = Objects.requireNonNull(tenantRoleRepository, "tenantRoleRepository");
    }

    public Map<String, Object> parseExtInfo(String extInfo) {
        if (extInfo != null && !extInfo.isEmpty()) {
            Map<String, Object> map = JSONUtils.parseMap(extInfo);
            if (map != null) {
                return map;
            }
        }
        return new LinkedHashMap<>();
    }

    public Long numberValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    public boolean isActiveAssignment(UserScopeRoleBO item) {
        return item != null
                && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0)
                && item.getTenantId() != null
                && item.getRoleId() != null;
    }

    public boolean isActiveTenant(TenantBO item) {
        return item != null
                && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    public boolean isTenantSuperRole(RoleBO role) {
        return role != null
                && ("tenant_super".equals(role.getCode()) || "super".equals(role.getCode()))
                && role.getStatus() != null && role.getStatus() == 1
                && (role.getDelFlag() == null || role.getDelFlag() == 0);
    }

    public boolean hasTenantAssignment(List<UserScopeRoleBO> assignments, Long tenantId) {
        return tenantId != null && assignments != null && assignments.stream()
                .anyMatch(item -> tenantId.equals(item.getTenantId()) && isActiveAssignment(item));
    }

    public boolean isDelegatedTenantContext(Map<String, Object> extInfo,
                                            List<UserScopeRoleBO> assignments,
                                            Long targetTenantId) {
        if (extInfo == null || !"PARENT_SUPER_ADMIN".equals(extInfo.get("switchMode"))) {
            return false;
        }
        Long homeTenantId = numberValue(extInfo.get("homeTenantId"));
        return homeTenantId != null
                && targetTenantId != null
                && tenantRepository.selectDescendantIds(new TenantId(homeTenantId)).contains(targetTenantId)
                && assignments != null
                && assignments.stream()
                .filter(item -> homeTenantId.equals(item.getTenantId()) && isActiveAssignment(item))
                .map(UserScopeRoleBO::getRoleId)
                .map(tenantRoleRepository::selectRoleById)
                .anyMatch(this::isTenantSuperRole);
    }

    public Long resolveCurrentTenantId(String extInfo, List<UserScopeRoleBO> assignments) {
        if (extInfo != null && !extInfo.isEmpty()) {
            try {
                Map<String, Object> map = JSONUtils.parseMap(extInfo);
                if (map != null && map.get("currentTenantId") instanceof Number) {
                    Long currentTenantId = ((Number) map.get("currentTenantId")).longValue();
                    if (hasTenantAssignment(assignments, currentTenantId)
                            || isDelegatedTenantContext(map, assignments, currentTenantId)) {
                        return currentTenantId;
                    }
                }
            } catch (Exception ignored) {
                // Invalid ext_info falls back to the first active assignment.
            }
        }
        if (assignments == null || assignments.isEmpty()) {
            return null;
        }
        return assignments.stream()
                .filter(this::isActiveAssignment)
                .map(UserScopeRoleBO::getTenantId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public RoleBO findTenantSuperRole(Long tenantId) {
        return tenantId == null ? null : tenantRoleRepository.selectTenantSuperRole(new TenantId(tenantId));
    }

    public Map<String, Object> buildExtInfo(String oldExtInfo, RoleBO currentRole,
                                             Long currentTenantId, LocalDateTime now,
                                             String loginIp) {
        Map<String, Object> extInfoMap = parseExtInfo(oldExtInfo);
        extInfoMap.put("lastLoginTime", now.toString());
        extInfoMap.put("lastLoginIp", loginIp);
        if (currentRole != null) {
            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("roleId", currentRole.getId());
            roleMap.put("roleName", currentRole.getName());
            roleMap.put("roleKey", currentRole.getCode());
            extInfoMap.put("currentRole", roleMap);
        }
        if (currentTenantId != null) {
            extInfoMap.put("currentTenantId", currentTenantId);
        }
        return extInfoMap;
    }

    public List<TenantContextVO> buildSubTenantList(List<UserScopeRoleBO> assignments,
                                                     Long currentTenantId) {
        if (assignments == null || assignments.isEmpty()) {
            return new ArrayList<>();
        }
        List<UserScopeRoleBO> filtered = currentTenantId == null
                ? assignments
                : assignments.stream()
                .filter(item -> currentTenantId.equals(item.getTenantId()) && isActiveAssignment(item))
                .toList();
        List<TenantContextVO> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (UserScopeRoleBO assignment : filtered) {
            if (!seen.add(assignment.getTenantId())) {
                continue;
            }
            TenantBO tenant = tenantRoleRepository.selectTenantById(new TenantId(assignment.getTenantId()));
            RoleBO role = tenantRoleRepository.selectRoleById(assignment.getRoleId());
            if (isActiveTenant(tenant) && isActiveRole(role)) {
                result.add(TenantContextVO.builder()
                        .tenantId(assignment.getTenantId())
                        .tenantName(tenant.getName())
                        .roleCode(role.getCode())
                        .build());
            }
        }
        return result;
    }

    public List<TenantInfoVO> buildTenantList(List<UserScopeRoleBO> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> tenantIds = assignments.stream()
                .filter(this::isActiveAssignment)
                .map(UserScopeRoleBO::getTenantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<TenantInfoVO> result = new ArrayList<>();
        for (Long tenantId : tenantIds) {
            TenantBO tenant = tenantRoleRepository.selectTenantById(new TenantId(tenantId));
            if (isActiveTenant(tenant)) {
                TenantInfoVO vo = new TenantInfoVO();
                vo.setId(tenant.getId());
                vo.setCode(tenant.getCode());
                vo.setName(tenant.getName());
                vo.setPathName(tenant.getName());
                result.add(vo);
            }
        }
        return result;
    }

    public List<TenantInfoVO> buildScopedTenantList(Long scopeRootTenantId,
                                                     Long returnTenantId) {
        Map<Long, TenantInfoVO> result = new LinkedHashMap<>();
        Set<Long> visibleTenantIds = new LinkedHashSet<>(
                tenantRepository.selectDescendantIds(new TenantId(scopeRootTenantId)));
        if (returnTenantId != null) {
            visibleTenantIds.add(returnTenantId);
        }
        for (TenantBO tenant : tenantRepository.selectAll()) {
            if (!isActiveTenant(tenant) || !visibleTenantIds.contains(tenant.getId())) {
                continue;
            }
            TenantInfoVO vo = new TenantInfoVO();
            vo.setId(tenant.getId());
            vo.setCode(tenant.getCode());
            vo.setName(tenant.getName());
            vo.setPathName(buildTenantPath(tenant.getId()));
            result.put(tenant.getId(), vo);
        }
        return new ArrayList<>(result.values());
    }

    public String buildTenantPath(Long tenantId) {
        List<TenantBO> all = tenantRepository.selectAll();
        Map<Long, TenantBO> byId = all.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(TenantBO::getId, item -> item, (first, ignored) -> first));
        LinkedList<String> names = new LinkedList<>();
        TenantBO current = byId.get(tenantId);
        Set<Long> visited = new HashSet<>();
        while (current != null && visited.add(current.getId())) {
            names.addFirst(current.getName());
            current = current.getParentId() == null ? null : byId.get(current.getParentId());
        }
        return String.join(" / ", names);
    }

    public RoleBO resolveCurrentRoleForTenant(Long roleId, List<RoleBO> roles,
                                               List<UserScopeRoleBO> assignments,
                                               Long tenantId) {
        if (roles == null || roles.isEmpty() || tenantId == null || assignments == null) {
            return null;
        }
        Set<Long> allowedRoleIds = assignments.stream()
                .filter(item -> tenantId.equals(item.getTenantId()) && isActiveAssignment(item))
                .map(UserScopeRoleBO::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return roles.stream()
                .filter(role -> allowedRoleIds.contains(role.getId()))
                .filter(this::isActiveRole)
                .filter(role -> roleId == null || roleId.equals(role.getId()))
                .findFirst()
                .orElseGet(() -> roles.stream()
                        .filter(role -> allowedRoleIds.contains(role.getId()))
                        .filter(this::isActiveRole)
                        .findFirst()
                        .orElse(null));
    }

    private boolean isActiveRole(RoleBO role) {
        return role != null
                && role.getStatus() != null && role.getStatus() == 1
                && (role.getDelFlag() == null || role.getDelFlag() == 0);
    }
}
