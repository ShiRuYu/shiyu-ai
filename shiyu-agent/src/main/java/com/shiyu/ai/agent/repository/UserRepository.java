package com.shiyu.ai.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.UserDO;
import com.shiyu.ai.agent.dal.mapper.UserMapper;
import com.shiyu.ai.agent.dal.mapper.UserRoleMapper;
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
     * 根据ID查询用户
     */
    public UserBO selectOneById(Long id) {
        UserDO userDO = userMapper.selectOneById(id);
        if (userDO == null) {
            return null;
        }
        
        UserBO userBO = MapstructUtils.convert(userDO, UserBO.class);
        
        // 查询用户的角色列表
        List<RoleDO> roleDOs = userRoleMapper.selectRolesByUserId(id);
        if (roleDOs != null && !roleDOs.isEmpty()) {
            List<RoleBO> roleBOs = MapstructUtils.convert(roleDOs, RoleBO.class);
            userBO.setRoles(roleBOs);
        }
        
        return userBO;
    }

    /**
     * 插入用户
     */
    public UserBO insert(UserBO userBO) {
        UserDO userDO = MapstructUtils.convert(userBO, UserDO.class);
        userDO.setCreateTime(new Date());
        userDO.setUpdateTime(new Date());
        userDO.setStatus("1");
        
        userMapper.insert(userDO);
        return MapstructUtils.convert(userDO, UserBO.class);
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
}
