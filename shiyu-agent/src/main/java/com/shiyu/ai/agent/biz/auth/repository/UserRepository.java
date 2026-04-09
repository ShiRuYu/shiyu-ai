package com.shiyu.ai.agent.biz.auth.repository;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.auth.UserDO;
import com.shiyu.ai.agent.dal.mapper.auth.UserMapper;
import com.shiyu.ai.agent.dal.mapper.auth.UserRoleMapper;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 用户数据仓储层
 */
@Component
public class UserRepository {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     * 分页查询用户列表
     */
    public Pair<Long, List<UserBO>> selectPage(Integer pageNo, Integer pageSize, String username) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        
        long total = userMapper.selectCountByQuery(queryWrapper);
        
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
        List<UserDO> userDOs = userMapper.selectListByQuery(queryWrapper);
        List<UserBO> userBOs = MapstructUtils.convert(userDOs, UserBO.class);
        
        return Pair.of(total, userBOs);
    }

    /**
     * 根据用户名查询用户
     */
    public UserBO selectByUsername(String username) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(UserDO::getUsername, username);
        UserDO userDO = userMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(userDO, UserBO.class);
    }

    /**
     * 根据ID查询用户
     */
    public UserBO selectById(Long id) {
        UserDO userDO = userMapper.selectOneById(id);
        return MapstructUtils.convert(userDO, UserBO.class);
    }

    /**
     * 创建用户
     */
    public UserBO insert(UserBO userBO) {
        UserDO userDO = MapstructUtils.convert(userBO, UserDO.class);
        userDO.setCreateTime(new Date());
        userDO.setUpdateTime(new Date());
        userMapper.insert(userDO);
        userBO.setId(userDO.getId());
        return userBO;
    }

    /**
     * 更新用户
     */
    public boolean update(UserBO userBO) {
        UserDO userDO = MapstructUtils.convert(userBO, UserDO.class);
        userDO.setUpdateTime(new Date());
        return userMapper.update(userDO) > 0;
    }

    /**
     * 删除用户
     */
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    /**
     * 根据用户ID查询角色列表（返回 DO）
     */
    public List<RoleDO> selectRolesByUserIdAsDO(Long userId) {
        return userRoleMapper.selectRolesByUserId(userId);
    }

    /**
     * 根据用户ID查询角色列表（返回 BO）
     */
    public List<RoleBO> selectRolesByUserId(Long userId) {
        List<RoleDO> roleDOs = userRoleMapper.selectRolesByUserId(userId);
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    /**
     * 根据用户名查询用户（包含角色信息）
     */
    public UserBO selectUserWithRolesByUsername(String username) {
        UserDO userDO = userMapper.selectUserWithRolesByUsername(username);
        return MapstructUtils.convert(userDO, UserBO.class);
    }

}
