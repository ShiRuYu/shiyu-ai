package com.shiyu.ai.agent.biz.auth.repository;

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

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserRepository {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    public Pair<Long, List<UserBO>> selectPage(Number pageNo, Number pageSize, String username) {
        QueryWrapper countWrapper = new QueryWrapper();
        if (username != null && !username.isEmpty()) {
            countWrapper.like("username", username);
        }
        long total = userMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }

        List<UserDO> userDOs = userMapper.selectListByQuery(queryWrapper);
        List<UserBO> userBOs = MapstructUtils.convert(userDOs, UserBO.class);

        return Pair.of(total, userBOs);
    }

    public UserBO selectByUsername(String username) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(UserDO::getUsername, username);
        UserDO userDO = userMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(userDO, UserBO.class);
    }

    public UserBO selectById(Long id) {
        UserDO userDO = userMapper.selectOneById(id);
        return MapstructUtils.convert(userDO, UserBO.class);
    }

    public UserBO insert(UserBO userBO) {
        UserDO userDO = MapstructUtils.convert(userBO, UserDO.class);
        userMapper.insertSelective(userDO);
        userBO.setId(userDO.getId());
        return userBO;
    }

    public boolean update(UserBO userBO) {
        UserDO userDO = MapstructUtils.convert(userBO, UserDO.class);
        userDO.setUpdateTime(LocalDateTime.now());
        return userMapper.update(userDO) > 0;
    }

    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    public List<RoleBO> selectRolesByUserId(Long userId) {
        List<RoleDO> roleDOs = userRoleMapper.selectRolesByUserId(userId);
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    public UserBO selectUserWithRolesByUsername(String username) {
        UserDO userDO = userMapper.selectUserWithRolesByUsername(username);
        return MapstructUtils.convert(userDO, UserBO.class);
    }
}
