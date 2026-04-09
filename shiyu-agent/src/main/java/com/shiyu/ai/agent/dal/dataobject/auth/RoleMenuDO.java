package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色菜单关联数据对象
 */
@Data
@Table(value = "role_menu")
public class RoleMenuDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;
}
