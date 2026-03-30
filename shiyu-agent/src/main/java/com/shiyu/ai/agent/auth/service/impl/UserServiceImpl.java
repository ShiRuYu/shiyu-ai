package com.shiyu.ai.agent.auth.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.auth.service.UserService;
import com.shiyu.ai.agent.dal.dataobject.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.UserDO;
import com.shiyu.ai.agent.dal.mapper.UserMapper;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.UserPageResponse;
import com.shiyu.ai.agent.domain.vo.UserVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类（模拟数据）
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserBO getUserDetail(Long userId) {
        log.info("获取用户详情，userId: {}", userId);
        UserDO userDO = userMapper.selectOneById(userId);
        if (userDO == null) {
            return null;
        }
        return MapstructUtils.convert(userDO, UserBO.class);
    }

    @Override
    public UserPageResponse getUserList(String username, Integer pageNo, Integer pageSize) {
        log.info("获取用户列表，username: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        // 构建查询条件
        List<UserDO> allUsers = userMapper.selectListByQuery(queryWrapper);
        
        // 分页
        int total = allUsers.size();
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        if (fromIndex >= total) {
            UserPageResponse response = new UserPageResponse();
            response.setPageData(new ArrayList<>());
            response.setTotal(0L);
            return response;
        }
        
        List<UserDO> pageData = allUsers.subList(fromIndex, toIndex);
        List<UserVO> userVOs = MapstructUtils.convert(pageData, UserVO.class);
        
        UserPageResponse response = new UserPageResponse();
        response.setPageData(userVOs);
        response.setTotal((long) total);
        
        return response;
    }

    @Override
    public boolean deleteUser(Long userId) {
        log.info("删除用户，userId: {}", userId);
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    public boolean updateUser(Long userId, UserBO userBO) {
        log.info("修改用户，userId: {}", userId);
        UserDO existingUser = userMapper.selectOneById(userId);
        if (existingUser == null) {
            return false;
        }
        
        // 更新用户信息
        UserDO updatedUser = MapstructUtils.convert(userBO, UserDO.class);
        updatedUser.setId(userId);
        updatedUser.setUpdateTime(new Date());
        
        return userMapper.update(updatedUser) > 0;
    }

    @Override
    public boolean resetUserPassword(Long userId, String password) {
        log.info("重置用户密码，userId: {}", userId);
        UserDO userDO = userMapper.selectOneById(userId);
        if (userDO == null) {
            return false;
        }
        
        // TODO: 实现密码重置逻辑
        log.info("用户 {} 密码已重置", userId);
        return true;
    }

    @Override
    public Long createUser(UserBO userBO) {
        log.info("新增用户");
        UserDO newUser = MapstructUtils.convert(userBO, UserDO.class);
        newUser.setCreateTime(new Date());
        newUser.setUpdateTime(new Date());
        newUser.setEnable(true);
        
        userMapper.insert(newUser);
        
        return newUser.getId();
    }
}
