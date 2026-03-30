package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.MenuDO;
import com.shiyu.ai.agent.domain.RoleDO;
import com.shiyu.ai.agent.domain.UserDO;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.UserPageResponse;
import com.shiyu.ai.agent.domain.vo.UserVO;
import com.shiyu.ai.agent.service.UserService;
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

    // 模拟用户数据
    private final Map<Long, UserDO> userDatabase = new HashMap<>();

    public UserServiceImpl() {
        // 初始化模拟数据
        initMockData();
    }

    private void initMockData() {
        // 创建角色数据
        RoleDO role1 = new RoleDO();
        role1.setId(1L);
        role1.setCode("SUPER_ADMIN");
        role1.setName("超级管理员");
        role1.setEnable(true);
        role1.setPermissionIds(new Long[]{1L, 2L, 3L, 4L, 5L});

        RoleDO role2 = new RoleDO();
        role2.setId(2L);
        role2.setCode("ROLE_QA");
        role2.setName("质检员");
        role2.setEnable(true);
        role2.setPermissionIds(new Long[]{1L, 2L, 3L, 4L, 5L, 9L, 10L, 11L, 12L, 14L, 15L});

        // 创建用户数据
        UserDO user1 = new UserDO();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setEnable(true);
        user1.setCreateTime(new Date());
        user1.setUpdateTime(new Date());
        user1.setNickName("Admin");
        user1.setGender(null);
        user1.setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif?imageView2/1/w/80/h/80");
        user1.setAddress(null);
        user1.setEmail(null);
        user1.setRoles(Arrays.asList(role1, role2));
        user1.setCurrentRole(role1);

        userDatabase.put(1L, user1);
    }

    @Override
    public UserBO getUserDetail(Long userId) {
        log.info("获取用户详情，userId: {}", userId);
        UserDO userDO = userDatabase.get(userId);
        if (userDO == null) {
            return null;
        }
        return MapstructUtils.convert(userDO, UserBO.class);
    }

    @Override
    public UserPageResponse getUserList(String username, Integer pageNo, Integer pageSize) {
        log.info("获取用户列表，username: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        
        List<UserDO> allUsers = new ArrayList<>(userDatabase.values());
        
        // 过滤用户名
        if (username != null && !username.isEmpty()) {
            String finalUsername = username;
            allUsers = allUsers.stream()
                    .filter(u -> u.getUsername().contains(finalUsername))
                    .collect(Collectors.toList());
        }
        
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
        UserDO removed = userDatabase.remove(userId);
        return removed != null;
    }

    @Override
    public boolean updateUser(Long userId, UserBO userBO) {
        log.info("修改用户，userId: {}", userId);
        UserDO existingUser = userDatabase.get(userId);
        if (existingUser == null) {
            return false;
        }
        
        // 更新用户信息
        UserDO updatedUser = MapstructUtils.convert(userBO, UserDO.class);
        updatedUser.setId(userId);
        updatedUser.setUpdateTime(new Date());
        userDatabase.put(userId, updatedUser);
        
        return true;
    }

    @Override
    public boolean resetUserPassword(Long userId, String password) {
        log.info("重置用户密码，userId: {}", userId);
        UserDO userDO = userDatabase.get(userId);
        if (userDO == null) {
            return false;
        }
        // 模拟密码重置
        log.info("用户 {} 密码已重置", userId);
        return true;
    }

    @Override
    public Long createUser(UserBO userBO) {
        log.info("新增用户");
        Long newId = userDatabase.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        
        UserDO newUser = MapstructUtils.convert(userBO, UserDO.class);
        newUser.setId(newId);
        newUser.setCreateTime(new Date());
        newUser.setUpdateTime(new Date());
        newUser.setEnable(true);
        
        userDatabase.put(newId, newUser);
        
        return newId;
    }
}
