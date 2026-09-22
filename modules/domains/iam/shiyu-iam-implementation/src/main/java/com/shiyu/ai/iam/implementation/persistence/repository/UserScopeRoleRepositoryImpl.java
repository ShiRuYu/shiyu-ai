package com.shiyu.ai.iam.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.mybatis.tenant.TenantQueryExecutor;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.UserScopeRoleMapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 用户 Scope 角色 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class UserScopeRoleRepositoryImpl
        implements com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository {

    /**
     * userScopeRoleMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private UserScopeRoleMapper userScopeRoleMapper;

    public List<UserScopeRoleBO> selectByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId is required for identity assignment lookup");
        }
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(() -> userScopeRoleMapper.selectByUserId(userId)),
                UserScopeRoleBO.class);
    }

    @Override
    public List<UserScopeRoleBO> selectByUserIds(List<Long> userIds) {
        return MapstructUtils.convert(
                userScopeRoleMapper.selectByUserIds(userIds), UserScopeRoleBO.class);
    }

    /** 插入用户-角色关联记录。 使用 insertSelective 忽略 null 字段，让数据库 DEFAULT 生效。 */
    public void insert(UserScopeRoleBO userScopeRole) {
        userScopeRoleMapper.insertSelective(
                MapstructUtils.convert(userScopeRole, UserScopeRoleDO.class));
    }

    public void deleteByUserIdAndTenantId(Long userId, TenantId tenantId) {
        userScopeRoleMapper.deleteByQuery(
                QueryWrapper.create()
                        .eq(UserScopeRoleDO::getUserId, userId)
                        .eq(UserScopeRoleDO::getTenantId, requireTenant(tenantId)));
    }

    public void deleteByUserIdRoleIdAndTenantId(Long userId, Long roleId, TenantId tenantId) {
        userScopeRoleMapper.deleteByQuery(
                QueryWrapper.create()
                        .eq(UserScopeRoleDO::getUserId, userId)
                        .eq(UserScopeRoleDO::getRoleId, roleId)
                        .eq(UserScopeRoleDO::getTenantId, requireTenant(tenantId)));
    }

    private static long requireTenant(TenantId tenantId) {
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required for user scope mutation");
        }
        TenantScope.requireMatchesIfBound(tenantId);
        return tenantId.value();
    }
}
