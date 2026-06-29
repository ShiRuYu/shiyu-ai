package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.repository.UserRepository;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.auth.bo.RoleBO;
import com.shiyu.ai.auth.bo.UserBO;
import com.shiyu.ai.auth.vo.UserPageResponse;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.common.core.utils.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 鐢ㄦ埛鏈嶅姟瀹炵幇绫?
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
        log.info("鑾峰彇鐢ㄦ埛璇︽儏锛寀serId: {}", userId);
        UserBO userBO = userRepository.selectById(userId);
        
        if (userBO != null) {
            // 鏌ヨ骞惰缃敤鎴疯鑹插垪琛?
            List<RoleBO> roles = userRepository.selectRolesByUserId(userId);
            userBO.setRoles(roles);

            // 浠?extInfo 涓В鏋愬綋鍓嶈鑹?
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
        log.info("鑾峰彇鐢ㄦ埛鍒楄〃锛寀sername: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        
        Pair<Long, List<UserBO>> result = userRepository.selectPage(pageNo, pageSize, username);
        List<UserVO> userVOs = MapstructUtils.convert(result.getRight(), UserVO.class);
        
        UserPageResponse response = new UserPageResponse();
        response.setItems(userVOs);
        response.setTotal(result.getLeft());
        
        return response;
    }

    @Override
    public boolean deleteUser(Long userId) {
        log.info("鍒犻櫎鐢ㄦ埛锛寀serId: {}", userId);
        return userRepository.deleteById(userId);
    }

    @Override
    public boolean updateUser(Long userId, UserBO userBO) {
        log.info("淇敼鐢ㄦ埛锛寀serId: {}", userId);
        UserBO existingUser = userRepository.selectById(userId);
        if (existingUser == null) {
            return false;
        }
        
        userBO.setId(userId);
        return userRepository.update(userBO);
    }

    @Override
    public boolean resetUserPassword(Long userId, String password) {
        log.info("閲嶇疆鐢ㄦ埛瀵嗙爜锛寀serId: {}", userId);
        UserBO userBO = userRepository.selectById(userId);
        if (userBO == null) {
            return false;
        }
        
        userBO.setPassword(PasswordUtils.encode(password));
        return userRepository.update(userBO);
    }

    @Override
    public Long createUser(UserBO userBO) {
        log.info("鏂板鐢ㄦ埛: {}", userBO.getUsername());
        if (userBO.getPassword() == null || userBO.getPassword().isBlank()) {
            userBO.setPassword(PasswordUtils.encode(PasswordUtils.DEFAULT_PASSWORD));
        } else {
            userBO.setPassword(PasswordUtils.encode(userBO.getPassword()));
        }
        UserBO createdUser = userRepository.insert(userBO);
        return createdUser.getId();
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("淇敼瀵嗙爜锛寀serId: {}", userId);
        UserBO userBO = userRepository.selectById(userId);
        if (userBO == null) {
            return false;
        }
        if (!PasswordUtils.matches(oldPassword, userBO.getPassword())) {
            log.warn("鏃у瘑鐮侀敊璇紝userId: {}", userId);
            return false;
        }
        userBO.setPassword(PasswordUtils.encode(newPassword));
        return userRepository.update(userBO);
    }
}
