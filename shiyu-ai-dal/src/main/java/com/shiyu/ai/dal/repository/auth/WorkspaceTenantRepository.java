package com.shiyu.ai.dal.repository.auth;

import com.shiyu.ai.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.dal.dataobject.auth.TenantDO;
import com.shiyu.ai.dal.dataobject.auth.WorkspaceDO;
import com.shiyu.ai.dal.mapper.auth.RoleMapper;
import com.shiyu.ai.dal.mapper.auth.TenantMapper;
import com.shiyu.ai.dal.mapper.auth.WorkspaceMapper;
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
