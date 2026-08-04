package com.shiyu.ai.dal.auth.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.TenantMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 租户/角色查询仓储
 * 提供租户和角色信息的查询（原 TenantRoleRepository 精简版）
 */
@Component
public class TenantRoleRepository implements com.shiyu.ai.auth.port.repository.TenantRoleRepository {

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private RoleMapper roleMapper;

    public TenantBO selectTenantById(Long tenantId) {
        return MapstructUtils.convert(tenantMapper.selectOneById(tenantId), TenantBO.class);
    }

    public RoleBO selectRoleById(Long roleId) {
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(
                () -> roleMapper.selectOneById(roleId)), RoleBO.class);
    }

    public RoleBO selectTenantSuperRole(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() ->
                roleMapper.selectOneByQuery(QueryWrapper.create()
                        .where(RoleDO::getTenantId).eq(tenantId)
                        .and(RoleDO::getCode).in("tenant_super", "super")
                        .and(RoleDO::getStatus).eq(1)
                        .and(RoleDO::getDelFlag).eq(0)
                        .orderBy(RoleDO::getId, true))), RoleBO.class);
    }

    public String selectTenantNameById(Long tenantId) {
        TenantDO tenant = tenantMapper.selectOneById(tenantId);
        return tenant != null ? tenant.getName() : "Unknown";
    }
}
