package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户角色关联数据对象
 */
@Data
@Table(value = "user_role")
public class UserRoleDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 租户ID
     */
    private Long tenantId;
}
