package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.UserMapper;
import com.shiyu.ai.dal.auth.mapper.UserScopeRoleMapper;
import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.dal.auth.bo.UserBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
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
    private UserScopeRoleMapper userWorkspaceRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    /**
     * 分页查询用户列表
     */
    public Pair<Long, List<UserBO>> selectPage(Number pageNo, Number pageSize, String username) {
        QueryWrapper countWrapper = new QueryWrapper();
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId != null) {
            countWrapper.from(UserDO.class)
                    .in(UserDO::getId, buildVisibleUserIdsQuery(currentTenantId));
        }
        if (username != null && !username.isEmpty()) {
            countWrapper.like("username", username);
        }
        long total = userMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        if (currentTenantId != null) {
            queryWrapper.from(UserDO.class)
                    .in(UserDO::getId, buildVisibleUserIdsQuery(currentTenantId));
        }
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
     * 使用用户 ID 子查询代替直接 join 用户角色关系表。
     * 一个用户在当前租户下有多个角色时，join 会把同一用户展开成多行，
     * 进而导致列表重复、总数错误以及分页被重复记录占用。
     */
    private QueryWrapper buildVisibleUserIdsQuery(Long currentTenantId) {
        return QueryWrapper.create()
                .select(UserScopeRoleDO::getUserId)
                .from(UserScopeRoleDO.class)
                .where(UserScopeRoleDO::getTenantId).eq(currentTenantId)
                .and(UserScopeRoleDO::getStatus).eq(1)
                .and(UserScopeRoleDO::getDelFlag).eq(0);
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
        userWorkspaceRoleMapper.deleteByQuery(QueryWrapper.create()
                .where(UserScopeRoleDO::getUserId).eq(id));
        return userMapper.deleteById(id) > 0;
    }

    /**
     * 根据用户ID查询角色列表（从 user_scope_role 去重获取）
     */
    public List<RoleBO> selectRolesByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create()
            .from(RoleDO.class)
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleDO::getId).eq(column(UserScopeRoleDO::getRoleId)))
            .where(UserScopeRoleDO::getUserId).eq(userId)
            .and(RoleDO::getStatus).eq(1)
            .and(RoleDO::getDelFlag).eq(0);
        qw.orderBy(RoleDO::getId);
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(qw);
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    /**
     * 根据用户名查询用户（包含角色信息）
     */

    /**
     * 根据邮箱查询用户
     */
    public UserBO selectByEmail(String email) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(UserDO::getEmail, email);
        UserDO userDO = userMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(userDO, UserBO.class);
    }

    /**
     * 根据用户名查询活跃用户（不含角色关联信息，方法名避免误导）
     * 如需角色信息请额外调用 selectRolesByUserId()
     */
    public UserBO selectActiveUserByUsername(String username) {
        QueryWrapper qw = QueryWrapper.create()
            .from(UserDO.class)
            .where(UserDO::getUsername).eq(username)
            .and(UserDO::getStatus).eq(1)
            .and(UserDO::getDelFlag).eq(0);
        UserDO userDO = userMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(userDO, UserBO.class);
    }

    /**
     * 校验用户是否属于当前租户作用域。
     * 该校验用于角色分配等写操作，不能只依赖前端传入的 userId。
     */
    public boolean isUserInScope(Long userId, Long currentTenantId) {
        if (userId == null || currentTenantId == null) {
            return false;
        }
        QueryWrapper qw = QueryWrapper.create()
                .where(UserDO::getId).eq(userId);
        qw.and(UserDO::getStatus).eq(1)
                .and(UserDO::getDelFlag).eq(0);
        QueryWrapper assignmentQuery = QueryWrapper.create()
                .from(UserScopeRoleDO.class)
                .where(UserScopeRoleDO::getUserId).eq(userId)
                .and(UserScopeRoleDO::getTenantId).eq(currentTenantId)
                .and(UserScopeRoleDO::getStatus).eq(1)
                .and(UserScopeRoleDO::getDelFlag).eq(0);
        return userMapper.selectCountByQuery(qw) > 0
                && userWorkspaceRoleMapper.selectCountByQuery(assignmentQuery) > 0;
    }

}
