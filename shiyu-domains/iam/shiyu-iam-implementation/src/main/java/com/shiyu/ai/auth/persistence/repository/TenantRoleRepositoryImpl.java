package com.shiyu.ai.auth.persistence.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.TenantDO;
import com.shiyu.ai.auth.persistence.mapper.RoleMapper;
import com.shiyu.ai.auth.persistence.mapper.TenantMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 租户/角色查询仓储
 * 提供租户和角色信息的查询（原 TenantRoleRepositoryImpl 精简版）
 */
@Component
public class TenantRoleRepositoryImpl implements com.shiyu.ai.auth.port.repository.TenantRoleRepository {

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private RoleMapper roleMapper;

    public TenantBO selectTenantById(TenantId tenantId) {
        return MapstructUtils.convert(tenantMapper.selectOneById(requireTenant(tenantId)), TenantBO.class);
    }

    public RoleBO selectRoleById(Long roleId) {
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(
                () -> roleMapper.selectOneById(roleId)), RoleBO.class);
    }

    @Override
    public RoleBO selectEnabledRoleByCode(TenantId tenantId, String roleCode) {
        long tenantValue = requireTenant(tenantId);
        if (roleCode == null || roleCode.isBlank()) {
            return null;
        }
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() ->
                roleMapper.selectOneByQuery(QueryWrapper.create()
                        .where(RoleDO::getTenantId).eq(tenantValue)
                        .and(RoleDO::getCode).eq(roleCode)
                        .and(RoleDO::getStatus).eq(1)
                        .and(RoleDO::getDelFlag).eq(0)
                        .orderBy(RoleDO::getId, true))), RoleBO.class);
    }

    @Override
    public RoleBO selectTenantSuperRole(TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() ->
                roleMapper.selectOneByQuery(QueryWrapper.create()
                        .where(RoleDO::getTenantId).eq(tenantValue)
                        .and(RoleDO::getCode).in("tenant_super", "super")
                        .and(RoleDO::getStatus).eq(1)
                        .and(RoleDO::getDelFlag).eq(0)
                        .orderBy(RoleDO::getId, true))), RoleBO.class);
    }

    public String selectTenantNameById(TenantId tenantId) {
        TenantDO tenant = tenantMapper.selectOneById(requireTenant(tenantId));
        return tenant != null ? tenant.getName() : "Unknown";
    }

    private static long requireTenant(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required for tenant role query");
        }
        return tenantId.value();
    }
}

