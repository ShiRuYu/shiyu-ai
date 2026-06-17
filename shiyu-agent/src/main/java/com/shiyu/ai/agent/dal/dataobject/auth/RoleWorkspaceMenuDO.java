package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色工作空间菜单关联数据对象
 */
@Data
@Table(value = "role_workspace_menu")
public class RoleWorkspaceMenuDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 工作空间ID
     */
    private Long workspaceId;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 租户ID
     */
    private Long tenantId;
}
