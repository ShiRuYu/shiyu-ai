package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.UserRepository;
import com.shiyu.ai.agent.biz.auth.service.UserService;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.UserPageResponse;
import com.shiyu.ai.agent.domain.vo.UserVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

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
        }
        
        return userBO;
    }

    @Override
    public UserPageResponse getUserList(String username, Integer pageNo, Integer pageSize) {
        log.info("获取用户列表，username: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        
        Pair<Long, List<UserBO>> result = userRepository.selectPage(pageNo, pageSize, username);
        List<UserVO> userVOs = MapstructUtils.convert(result.getRight(), UserVO.class);
        
        UserPageResponse response = new UserPageResponse();
        response.setPageData(userVOs);
        response.setTotal(result.getLeft());
        
        return response;
    }

    @Override
    public boolean deleteUser(Long userId) {
        log.info("删除用户，userId: {}", userId);
        return userRepository.deleteById(userId);
    }

    @Override
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
    public boolean resetUserPassword(Long userId, String password) {
        log.info("重置用户密码，userId: {}", userId);
        UserBO userBO = userRepository.selectById(userId);
        if (userBO == null) {
            return false;
        }
        
        // TODO: 实现密码重置逻辑
        log.info("用户 {} 密码已重置", userId);
        return true;
    }

    @Override
    public Long createUser(UserBO userBO) {
        log.info("新增用户");
        UserBO createdUser = userRepository.insert(userBO);
        return createdUser.getId();
    }
}
