package com.shiyu.ai.dal.auth.repository;

import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.dataobject.WorkspaceDO;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.TenantMapper;
import com.shiyu.ai.dal.auth.mapper.WorkspaceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceTenantRepository {

    @Resource
    private WorkspaceMapper workspaceMapper;

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private RoleMapper roleMapper;

    public WorkspaceDO selectWorkspaceById(Long workspaceId) {
        return workspaceMapper.selectOneById(workspaceId);
    }

    public TenantDO selectTenantById(Long tenantId) {
        return tenantMapper.selectOneById(tenantId);
    }

    public RoleDO selectRoleById(Long roleId) {
        return roleMapper.selectOneById(roleId);
    }
}
