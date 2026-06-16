package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Table(value = "user_workspace_role")
public class UserWorkspaceRoleDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long workspaceId;

    private Long roleId;

    private Long tenantId;
}
