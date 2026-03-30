package com.shiyu.ai.agent.controller.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色请求对象
 */
@Data
public class RoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 是否启用（1 是 0 否）
     */
    private Boolean enable;

    /**
     * 权限 ID 列表
     */
    private List<Long> permissionIds;
}
