package com.shiyu.ai.agent.dal.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户角色关联表数据对象
 */
@Data
@Table(value = "user_role")
public class UserRoleDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 角色 ID
     */
    private Long roleId;
}
