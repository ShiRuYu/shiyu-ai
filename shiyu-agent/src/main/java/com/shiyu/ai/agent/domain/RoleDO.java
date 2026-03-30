package com.shiyu.ai.agent.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色数据对象（模拟数据）
 */
@Data
public class RoleDO implements Serializable {

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
