package com.shiyu.ai.iam.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.UserScopeRoleMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code UserScopeRoleRepositoryImpl} 实现平台模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class UserScopeRoleRepositoryImpl
        implements com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository {

    /**
     * userScopeRoleMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private UserScopeRoleMapper userScopeRoleMapper;

    public List<UserScopeRoleBO> selectByUserId(Long userId) {
        return MapstructUtils.convert(
                userScopeRoleMapper.selectByUserId(userId), UserScopeRoleBO.class);
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
        return tenantId.value();
    }
}
