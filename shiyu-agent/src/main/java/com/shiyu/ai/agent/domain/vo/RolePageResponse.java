package com.shiyu.ai.agent.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色分页列表响应对象
 */
@Data
public class RolePageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色列表
     */
    private List<RoleVO> pageData;

    /**
     * 总数
     */
    private Long total;
}
