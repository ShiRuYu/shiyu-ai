package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 角色和部门关联 sys_role_dept
 *
 */

@Data
@Table("sys_role_dept")
public class SysRoleDeptDO {

    /**
     * 角色ID
     */
    @Id
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;

}
