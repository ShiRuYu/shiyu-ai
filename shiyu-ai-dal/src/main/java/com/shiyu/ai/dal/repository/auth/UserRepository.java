package com.shiyu.ai.dal.repository.auth;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.dal.dataobject.auth.UserDO;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.mapper.auth.RoleMapper;
import com.shiyu.ai.dal.mapper.auth.UserMapper;
import com.shiyu.ai.dal.mapper.auth.UserWorkspaceRoleMapper;
import com.shiyu.ai.auth.bo.RoleBO;
import com.shiyu.ai.auth.bo.UserBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 用户数据仓储层
 */
@Component
public class UserRepository {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserWorkspaceRoleMapper userWorkspaceRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    /**
     * 分页查询用户列表
     */
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
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        userMapper.insertSelective(userDO);
        userBO.setId(userDO.getId());
        return userBO;
    }

    /**
     * 更新用户
     */
    public boolean update(UserBO userBO) {
        UserDO userDO = MapstructUtils.convert(userBO, UserDO.class);
        userDO.setUpdateTime(LocalDateTime.now());
        return userMapper.update(userDO) > 0;
    }

    /**
     * 删除用户
     */
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    /**
     * 根据用户ID查询角色列表（从 user_workspace_role 去重获取）
     */
    public List<RoleBO> selectRolesByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create()
            .from(RoleDO.class)
            .innerJoin(UserWorkspaceRoleDO.class)
                .on(column(RoleDO::getId).eq(column(UserWorkspaceRoleDO::getRoleId)))
            .where(UserWorkspaceRoleDO::getUserId).eq(userId)
            .and(RoleDO::getStatus).eq("1")
            .and(RoleDO::getDelFlag).eq(0);
        qw.orderBy(RoleDO::getId);
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(qw);
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    /**
     * 根据用户名查询用户（包含角色信息）
     */
    public UserBO selectUserWithRolesByUsername(String username) {
        QueryWrapper qw = QueryWrapper.create()
            .from(UserDO.class)
            .where(UserDO::getUsername).eq(username)
            .and(UserDO::getStatus).eq("1")
            .and(UserDO::getDelFlag).eq(0);
        UserDO userDO = userMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(userDO, UserBO.class);
    }

}
