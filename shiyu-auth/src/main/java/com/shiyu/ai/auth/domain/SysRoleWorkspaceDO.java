package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 角色和工作空间关联 sys_role_workspace
 *
 */

@Data
@Table("sys_role_workspace")
public class SysRoleWorkspaceDO {

    /**
     * 角色ID
     */
    @Id
    private Long roleId;

    /**
     * 工作空间ID
     */
    private Long workspaceId;

}
