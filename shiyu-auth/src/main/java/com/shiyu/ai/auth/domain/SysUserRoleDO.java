package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 *
 */

@Data
@Table("sys_user_role")
public class SysUserRoleDO {

    /**
     * 用户ID
     */
    @Id
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

}
