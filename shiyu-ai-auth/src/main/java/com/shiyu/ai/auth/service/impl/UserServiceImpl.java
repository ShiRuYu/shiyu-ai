package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.auth.repository.UserRepository;
import com.shiyu.ai.dal.auth.repository.RoleRepository;
import com.shiyu.ai.dal.auth.repository.UserScopeRoleRepository;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.dal.auth.bo.UserBO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.auth.vo.UserTenantAssignmentVO;
import com.shiyu.ai.auth.request.UserTenantRoleRequest;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.common.core.utils.PasswordUtils;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserScopeRoleRepository userScopeRoleRepository;
    private final com.shiyu.ai.dal.auth.repository.TenantRepository tenantRepository;
    private final com.shiyu.ai.dal.auth.repository.TenantRoleRepository tenantRoleRepository;
    private final MenuService menuService;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserScopeRoleRepository userScopeRoleRepository,
                           com.shiyu.ai.dal.auth.repository.TenantRepository tenantRepository,
                           com.shiyu.ai.dal.auth.repository.TenantRoleRepository tenantRoleRepository,
                           MenuService menuService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userScopeRoleRepository = userScopeRoleRepository;
        this.tenantRepository = tenantRepository;
        this.tenantRoleRepository = tenantRoleRepository;
        this.menuService = menuService;
    }

    @Override
    public UserBO getUserDetail(Long userId) {
        log.info("获取用户详情，userId: {}", userId);
        UserBO userBO = userRepository.selectById(userId);
        
        if (userBO != null) {
            // 查询并设置用户角色列表
            Long currentTenantId = LoginContextHolder.getCurrentTenantId();
            Map<String, Object> parsedExtInfo = userBO.getExtInfo() == null
                    ? null : JSONUtils.parseObject(userBO.getExtInfo(), Map.class);
            Map<String, Object> extInfoMap = parsedExtInfo == null ? Map.of() : parsedExtInfo;
            boolean parentSuperAdminSwitch = LoginContextHolder.isParentSuperAdminSwitch()
                    || "PARENT_SUPER_ADMIN".equals(extInfoMap.get("switchMode"));
            if (parentSuperAdminSwitch) {
                RoleDO delegatedRoleDO = tenantRoleRepository.selectTenantSuperRole(currentTenantId);
                RoleBO delegatedRole = delegatedRoleDO == null
                        ? null : MapstructUtils.convert(delegatedRoleDO, RoleBO.class);
                if (delegatedRole != null
                        && Objects.equals(currentTenantId, delegatedRole.getTenantId())
                        && ("tenant_super".equals(delegatedRole.getCode())
                        || "super".equals(delegatedRole.getCode()))) {
                    userBO.setRoles(List.of(delegatedRole));
                    userBO.setRoleIds(List.of(delegatedRole.getId()));
                    userBO.setCurrentRole(delegatedRole);
                    return userBO;
                }
            }
            List<UserScopeRoleDO> assignments = userScopeRoleRepository.selectByUserId(userId);
            List<Long> currentRoleIds = assignments.stream()
                    .filter(item -> currentTenantId == null
                            || Objects.equals(currentTenantId, item.getTenantId()))
                    .filter(this::isActiveAssignment)
                    .map(UserScopeRoleDO::getRoleId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            List<RoleBO> roles = userRepository.selectRolesByUserId(userId).stream()
                    .filter(role -> currentRoleIds.contains(role.getId()))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(
                                    RoleBO::getId,
                                    role -> role,
                                    (first, ignored) -> first,
                                    LinkedHashMap::new),
                            map -> new ArrayList<>(map.values())));
            userBO.setRoles(roles);
            userBO.setRoleIds(currentRoleIds);

            // 当前角色必须以当前租户的授权关系为准，不能直接信任 extInfo 中的历史值。
            RoleBO currentRole = null;
            if (extInfoMap != null && extInfoMap.get("currentRole") instanceof Map<?, ?> roleMap) {
                Object roleId = roleMap.get("roleId");
                if (roleId instanceof Number && currentRoleIds.contains(((Number) roleId).longValue())) {
                    currentRole = roles.stream()
                            .filter(role -> Objects.equals(((Number) roleId).longValue(), role.getId()))
                            .findFirst()
                            .orElse(null);
                }
            }
            if (currentRole == null && !roles.isEmpty()) {
                currentRole = roles.get(0);
            }
            userBO.setCurrentRole(currentRole);
        }
        
        return userBO;
    }

    @Override
    public PageData<UserVO> getUserList(String username, Number pageNum, Number pageSize) {
        log.info("获取用户列表，username: {}, pageNum: {}, pageSize: {}", username, pageNum, pageSize);
        
        Pair<Long, List<UserBO>> result = userRepository.selectPage(pageNum, pageSize, username);
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        Map<Long, List<Long>> userRoleIds = new java.util.HashMap<>();
        if (currentTenantId != null) {
                result.getRight().forEach(user -> {
                List<Long> roleIds = userScopeRoleRepository.selectByUserId(user.getId()).stream()
                        .filter(item -> Objects.equals(currentTenantId, item.getTenantId())
                                && item.getStatus() != null && item.getStatus() == 1
                                && (item.getDelFlag() == null || item.getDelFlag() == 0))
                        .map(UserScopeRoleDO::getRoleId)
                        .distinct()
                        .toList();
                userRoleIds.put(user.getId(), roleIds);
            });
        }
        List<UserVO> userVOs = MapstructUtils.convert(result.getRight(), UserVO.class);
        userVOs.forEach(user -> user.setRoleIds(
                userRoleIds.getOrDefault(user.getId(), List.of())));
        
        return new PageData<>(userVOs, result.getLeft());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        log.info("删除用户，userId: {}", userId);
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !userRepository.isUserInScope(userId, currentTenantId)) {
            return false;
        }
        return userRepository.deleteById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Long userId, UserBO userBO, Long[] roleIds, Long targetTenantId) {
        log.info("修改用户，userId: {}", userId);
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !userRepository.isUserInScope(userId, currentTenantId)) {
            return false;
        }
        UserBO existingUser = userRepository.selectById(userId);
        if (existingUser == null) {
            return false;
        }

        // 更新接口允许只提交变更字段，避免 null 覆盖已有资料。
        if (userBO.getUsername() == null) userBO.setUsername(existingUser.getUsername());
        if (userBO.getPassword() == null || userBO.getPassword().isBlank()) {
            userBO.setPassword(existingUser.getPassword());
        }
        if (userBO.getNickName() == null) userBO.setNickName(existingUser.getNickName());
        if (userBO.getGender() == null) userBO.setGender(existingUser.getGender());
        if (userBO.getAvatar() == null) userBO.setAvatar(existingUser.getAvatar());
        if (userBO.getAddress() == null) userBO.setAddress(existingUser.getAddress());
        if (userBO.getEmail() == null) userBO.setEmail(existingUser.getEmail());
        if (userBO.getPhone() == null) userBO.setPhone(existingUser.getPhone());
        if (userBO.getRemark() == null) userBO.setRemark(existingUser.getRemark());
        if (userBO.getStatus() == null) userBO.setStatus(existingUser.getStatus());
        if (userBO.getDelFlag() == null) userBO.setDelFlag(existingUser.getDelFlag());
        if (userBO.getExtInfo() == null) userBO.setExtInfo(existingUser.getExtInfo());

        userBO.setId(userId);
        boolean updated = userRepository.update(userBO);
        if (updated && roleIds != null) {
            syncUserRoles(userId, roleIds, targetTenantId);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetUserPassword(Long userId, String password) {
        log.info("重置用户密码，userId: {}", userId);
        UserBO userBO = userRepository.selectById(userId);
        if (userBO == null) {
            return null;
        }

        String newPassword = (password == null || password.isBlank())
                ? PasswordUtils.generateRandomPassword()
                : password;
        userBO.setPassword(PasswordUtils.encode(newPassword));
        boolean success = userRepository.update(userBO);
        
        if (success) {
            // 🔐 重置密码后踢出用户所有会话，强制重新登录
            SaTokenHelper.getInstance().logout(userId);
            SaTokenHelper.clearLoginUserSession();
            log.info("重置密码后已踢出用户会话: userId={}", userId);
        }
        
        return success ? newPassword : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createUser(UserBO userBO, Long[] roleIds, Long targetTenantId) {
        log.info("新增用户: {}", userBO.getUsername());
        String plainPassword = null;
        if (userBO.getPassword() == null || userBO.getPassword().isBlank()) {
            plainPassword = PasswordUtils.generateDefaultPassword();
            userBO.setPassword(PasswordUtils.encode(plainPassword));
        } else {
            userBO.setPassword(PasswordUtils.encode(userBO.getPassword()));
        }
        UserBO createdUser = userRepository.insert(userBO);
        if (roleIds != null) {
            syncUserRoles(createdUser.getId(), roleIds, targetTenantId);
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", createdUser.getId());
        result.put("plainPassword", plainPassword);
        return result;
    }

    @Override
    public List<UserTenantAssignmentVO> getTenantAssignments(Long userId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !canManageUser(userId, currentTenantId)) {
            return List.of();
        }
        List<Long> accessibleTenantIds = tenantRepository.selectDescendantIds(currentTenantId);
        List<UserTenantAssignmentVO> result = new ArrayList<>();
        for (UserScopeRoleDO assignment : userScopeRoleRepository.selectByUserId(userId)) {
            if (!isActiveAssignment(assignment)
                    || !accessibleTenantIds.contains(assignment.getTenantId())) {
                continue;
            }
            var tenant = tenantRoleRepository.selectTenantById(assignment.getTenantId());
            RoleDO role = tenantRoleRepository.selectRoleById(assignment.getRoleId());
            if (tenant == null || role == null || !isActiveTenant(tenant)
                    || role.getStatus() == null || role.getStatus() != 1) {
                continue;
            }
            UserTenantAssignmentVO item = new UserTenantAssignmentVO();
            item.setTenantId(tenant.getId());
            item.setTenantName(tenant.getName());
            item.setRoleId(role.getId());
            item.setRoleName(role.getName());
            item.setRoleCode(role.getCode());
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceTenantAssignments(Long userId, List<UserTenantRoleRequest> assignments) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !canManageUser(userId, currentTenantId)) {
            return false;
        }

        List<UserTenantRoleRequest> target = assignments == null ? List.of() : assignments.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getTenantId() != null && item.getRoleId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                item -> item.getTenantId() + ":" + item.getRoleId(),
                                item -> item,
                                (first, ignored) -> first,
                                LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())));
        for (UserTenantRoleRequest item : target) {
            if (!isTenantManageable(currentTenantId, item.getTenantId())) {
                return false;
            }
            var tenant = tenantRoleRepository.selectTenantById(item.getTenantId());
            if (!isActiveTenant(tenant)
                    || !roleRepository.isRoleOwnedByTenant(
                            item.getRoleId(), item.getTenantId())) {
                return false;
            }
        }

        /*
         * 目标租户是独立作用域。父租户管理员给子租户分配角色时，只能
         * 替换目标子租户的关系，不能把父租户自己的超级管理员关系删掉，
         * 否则当前用户会失去切回父租户的能力。
         */
        target.stream()
                .map(UserTenantRoleRequest::getTenantId)
                .distinct()
                .forEach(tenantId -> userScopeRoleRepository.deleteByQuery(
                        QueryWrapper.create()
                                .eq(UserScopeRoleDO::getUserId, userId)
                                .eq(UserScopeRoleDO::getTenantId, tenantId)));

        for (UserTenantRoleRequest item : target) {
            UserScopeRoleDO assignment = new UserScopeRoleDO();
            assignment.setUserId(userId);
            assignment.setRoleId(item.getRoleId());
            // 角色和用户授权关系归属于目标租户，父租户仅负责授权操作。
            assignment.setTenantId(item.getTenantId());
            userScopeRoleRepository.insert(assignment);
        }
        menuService.evictRouteMenuCache(userId);
        return true;
    }

    /**
     * 当前操作者是否可以管理指定用户。
     *
     * 父租户超级管理员切换到子租户后，用户本身通常没有该子租户的
     * user_scope_role 记录（当前用户是被委托为子租户超管），因此不能
     * 只用 isUserInScope(currentTenantId) 判断。
     */
    private boolean canManageUser(Long userId, Long currentTenantId) {
        if (userId == null || currentTenantId == null) {
            return false;
        }
        if (userRepository.isUserInScope(userId, currentTenantId)) {
            return true;
        }
        if (!LoginContextHolder.isParentSuperAdminSwitch()) {
            return false;
        }
        Long homeTenantId = LoginContextHolder.getHomeTenantId();
        return homeTenantId != null
                && userRepository.isUserInScope(userId, homeTenantId);
    }

    /**
     * 当前租户只能操作自身，或由当前租户超级管理员操作其后代租户。
     * 兄弟租户和其他根租户永远不在可管理范围内。
     */
    private boolean isTenantManageable(Long currentTenantId, Long targetTenantId) {
        if (currentTenantId == null || targetTenantId == null) {
            return false;
        }
        if (Objects.equals(currentTenantId, targetTenantId)) {
            return true;
        }
        if (!LoginContextHolder.isSuperAdmin()) {
            return false;
        }
        return tenantRepository.selectDescendantIds(currentTenantId).contains(targetTenantId);
    }

    private boolean isActiveAssignment(UserScopeRoleDO item) {
        return (item.getStatus() == null || item.getStatus() == 1)
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    private boolean isActiveTenant(com.shiyu.ai.dal.auth.dataobject.TenantDO tenant) {
        return tenant != null && tenant.getStatus() != null && tenant.getStatus() == 1
                && (tenant.getDelFlag() == null || tenant.getDelFlag() == 0);
    }

    /**
     * roleIds 不属于 users 表，必须同步到当前租户作用域下的关系表。
     */
    private void syncUserRoles(Long userId, Long[] roleIds, Long requestedTenantId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new IllegalStateException("当前没有租户作用域，无法分配角色");
        }
        Long targetTenantId = requestedTenantId == null ? currentTenantId : requestedTenantId;
        if (!Objects.equals(currentTenantId, targetTenantId)) {
            throw new IllegalArgumentException("目标租户不在当前租户可管理范围内");
        }
        if (userRepository.selectById(userId) == null) {
            throw new IllegalArgumentException("用户不属于当前租户作用域");
        }

        List<Long> targetRoleIds = roleIds == null
                ? List.of()
                : java.util.Arrays.stream(roleIds)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();

        for (Long roleId : targetRoleIds) {
            if (!roleRepository.isRoleOwnedByTenant(roleId, targetTenantId)) {
                throw new IllegalArgumentException("角色不属于目标租户: " + roleId);
            }
        }

        QueryWrapper deleteWrapper = QueryWrapper.create()
                .eq(UserScopeRoleDO::getUserId, userId)
                .eq(UserScopeRoleDO::getTenantId, targetTenantId);
        userScopeRoleRepository.deleteByQuery(deleteWrapper);

        for (Long roleId : targetRoleIds) {
            UserScopeRoleDO assignment = new UserScopeRoleDO();
            assignment.setUserId(userId);
            assignment.setRoleId(roleId);
            assignment.setTenantId(targetTenantId);
            userScopeRoleRepository.insert(assignment);
        }

        menuService.evictRouteMenuCache(userId);
        log.info("用户角色同步成功, userId={}, tenantId={}, roleIds={}",
                userId, targetTenantId, targetRoleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码，userId: {}", userId);
        UserBO userBO = userRepository.selectById(userId);
        if (userBO == null) {
            return false;
        }
        if (!PasswordUtils.matches(oldPassword, userBO.getPassword())) {
            log.warn("旧密码错误，userId: {}", userId);
            return false;
        }
        userBO.setPassword(PasswordUtils.encode(newPassword));
        boolean success = userRepository.update(userBO);
        
        if (success) {
            // 🔐 修改密码后踢出用户所有会话，强制重新登录
            SaTokenHelper.getInstance().logout(userId);
            SaTokenHelper.clearLoginUserSession();
            log.info("修改密码后已踢出用户会话: userId={}", userId);
        }
        
        return success;
    }
}
