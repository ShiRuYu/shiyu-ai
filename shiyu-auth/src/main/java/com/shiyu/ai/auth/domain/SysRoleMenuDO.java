package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 角色和菜单关联 sys_role_menu
 *
 */

@Data
@Table("sys_role_menu")
public class SysRoleMenuDO {

    /**
     * 角色ID
     */
    @Id
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

}
