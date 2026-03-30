package com.shiyu.ai.agent.domain.vo;

import com.shiyu.ai.agent.domain.bo.RoleBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色视图对象
 */
@Data
@AutoMapper(target = RoleBO.class)
public class RoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    private Long id;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 权限 ID 列表
     */
    private Long[] permissionIds;
}
