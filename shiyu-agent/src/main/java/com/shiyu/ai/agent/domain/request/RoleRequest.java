package com.shiyu.ai.agent.domain.request;

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
     * 状态（1正常 0停用）
     */
    private String status;
}
