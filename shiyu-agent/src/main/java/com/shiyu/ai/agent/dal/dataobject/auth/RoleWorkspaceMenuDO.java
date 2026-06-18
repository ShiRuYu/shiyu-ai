package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("role_workspace_menu")
public class RoleWorkspaceMenuDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long workspaceId;

    private Long menuId;
}
