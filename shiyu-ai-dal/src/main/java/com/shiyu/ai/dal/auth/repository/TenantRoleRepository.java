package com.shiyu.ai.dal.auth.repository;

import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.TenantMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 租户/角色查询仓储
 * 提供租户和角色信息的查询（原 TenantRoleRepository 精简版）
 */
@Component
public class TenantRoleRepository {

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private RoleMapper roleMapper;

    public TenantDO selectTenantById(Long tenantId) {
        return tenantMapper.selectOneById(tenantId);
    }

    public RoleDO selectRoleById(Long roleId) {
        return roleMapper.selectOneById(roleId);
    }

    public String selectTenantNameById(Long tenantId) {
        TenantDO tenant = tenantMapper.selectOneById(tenantId);
        return tenant != null ? tenant.getName() : "Unknown";
    }
}
