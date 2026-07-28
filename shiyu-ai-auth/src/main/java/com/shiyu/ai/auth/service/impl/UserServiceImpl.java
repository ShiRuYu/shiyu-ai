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
import com.shiyu.ai.auth.vo.UserPageResponse;
import com.shiyu.ai.auth.vo.UserVO;
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

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserScopeRoleRepository userScopeRoleRepository;
    private final MenuService menuService;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserScopeRoleRepository userScopeRoleRepository,
                           MenuService menuService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userScopeRoleRepository = userScopeRoleRepository;
        this.menuService = menuService;
    }

    @Override
    public UserBO getUserDetail(Long userId) {
        log.info("获取用户详情，userId: {}", userId);
        UserBO userBO = userRepository.selectById(userId);
        
        if (userBO != null) {
            // 查询并设置用户角色列表
            List<RoleBO> roles = userRepository.selectRolesByUserId(userId);
            userBO.setRoles(roles);

            // 从 extInfo 中解析当前角色
            if (userBO.getExtInfo() != null && !userBO.getExtInfo().isEmpty()) {
                Map<?, ?> extInfoMap = JSONUtils.parseObject(userBO.getExtInfo(), Map.class);
                if (extInfoMap != null) {
                    Map<?, ?> roleMap = (Map<?, ?>) extInfoMap.get("currentRole");
                    if (roleMap != null) {
                        RoleBO currentRole = new RoleBO();
                        Object roleId = roleMap.get("roleId");
                        if (roleId instanceof Number) {
                            currentRole.setId(((Number) roleId).longValue());
                        }
                        currentRole.setName((String) roleMap.get("roleName"));
                        currentRole.setCode((String) roleMap.get("roleKey"));
                        userBO.setCurrentRole(currentRole);
                    }
                }
            }
        }
        
        return userBO;
    }

    @Override
    public UserPageResponse getUserList(String username, Number pageNo, Number pageSize) {
        log.info("获取用户列表，username: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        
        Pair<Long, List<UserBO>> result = userRepository.selectPage(pageNo, pageSize, username);
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        Map<Long, List<Long>> userRoleIds = new java.util.HashMap<>();
        if (currentTenantId != null) {
            result.getRight().forEach(user -> {
                List<Long> roleIds = userScopeRoleRepository.selectByUserId(user.getId()).stream()
                        .filter(item -> currentTenantId.equals(item.getTenantId())
                                && currentTenantId.equals(item.getScopedTenantId())
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
        
        UserPageResponse response = new UserPageResponse();
        response.setItems(userVOs);
        response.setTotal(result.getLeft());
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        log.info("删除用户，userId: {}", userId);
        return userRepository.deleteById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Long userId, UserBO userBO, Long[] roleIds) {
        log.info("修改用户，userId: {}", userId);
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
            syncUserRoles(userId, roleIds);
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
    public Map<String, Object> createUser(UserBO userBO, Long[] roleIds) {
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
            syncUserRoles(createdUser.getId(), roleIds);
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", createdUser.getId());
        result.put("plainPassword", plainPassword);
        return result;
    }

    /**
     * roleIds 不属于 users 表，必须同步到当前租户作用域下的关系表。
     */
    private void syncUserRoles(Long userId, Long[] roleIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new IllegalStateException("当前没有租户作用域，无法分配角色");
        }
        if (!userRepository.isUserInScope(userId, currentTenantId)) {
            throw new IllegalArgumentException("用户不属于当前租户作用域");
        }

        List<Long> targetRoleIds = roleIds == null
                ? List.of()
                : java.util.Arrays.stream(roleIds)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();

        for (Long roleId : targetRoleIds) {
            if (!roleRepository.isRoleInScope(roleId, currentTenantId)) {
                throw new IllegalArgumentException("角色不属于当前租户作用域: " + roleId);
            }
        }

        QueryWrapper deleteWrapper = QueryWrapper.create()
                .eq(UserScopeRoleDO::getUserId, userId)
                .eq(UserScopeRoleDO::getTenantId, currentTenantId)
                .eq(UserScopeRoleDO::getScopedTenantId, currentTenantId);
        userScopeRoleRepository.deleteByQuery(deleteWrapper);

        for (Long roleId : targetRoleIds) {
            UserScopeRoleDO assignment = new UserScopeRoleDO();
            assignment.setUserId(userId);
            assignment.setRoleId(roleId);
            assignment.setTenantId(currentTenantId);
            assignment.setScopedTenantId(currentTenantId);
            userScopeRoleRepository.insert(assignment);
        }

        menuService.evictRouteMenuCache(userId);
        log.info("用户角色同步成功, userId={}, tenantId={}, roleIds={}",
                userId, currentTenantId, targetRoleIds);
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
