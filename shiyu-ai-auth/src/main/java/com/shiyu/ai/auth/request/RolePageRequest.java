package com.shiyu.ai.auth.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色分页查询请求
 */
@Data
public class RolePageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private Integer pageNo;

    private Integer pageSize;
}
