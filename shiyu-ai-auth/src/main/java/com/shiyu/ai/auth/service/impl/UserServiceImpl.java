package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.auth.repository.UserRepository;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.dal.auth.bo.UserBO;
import com.shiyu.ai.auth.vo.UserPageResponse;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.common.core.utils.PasswordUtils;
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

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        List<UserVO> userVOs = MapstructUtils.convert(result.getRight(), UserVO.class);
        
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
    public boolean updateUser(Long userId, UserBO userBO) {
        log.info("修改用户，userId: {}", userId);
        UserBO existingUser = userRepository.selectById(userId);
        if (existingUser == null) {
            return false;
        }
        
        userBO.setId(userId);
        return userRepository.update(userBO);
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
    public Map<String, Object> createUser(UserBO userBO) {
        log.info("新增用户: {}", userBO.getUsername());
        String plainPassword = null;
        if (userBO.getPassword() == null || userBO.getPassword().isBlank()) {
            plainPassword = PasswordUtils.generateDefaultPassword();
            userBO.setPassword(PasswordUtils.encode(plainPassword));
        } else {
            userBO.setPassword(PasswordUtils.encode(userBO.getPassword()));
        }
        UserBO createdUser = userRepository.insert(userBO);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", createdUser.getId());
        result.put("plainPassword", plainPassword);
        return result;
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
