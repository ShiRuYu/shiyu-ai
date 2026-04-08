package com.shiyu.ai.agent.dal.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色菜单关联表数据对象
 */
@Data
@Table(value = "role_menu")
public class RoleMenuDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 菜单 ID
     */
    private Long menuId;
}
