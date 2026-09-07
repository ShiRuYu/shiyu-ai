package com.shiyu.ai.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 租户上下文 VO
 */
@Data
@Builder
public class TenantContextVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 租户名称 */
    private String tenantName;

    /** 角色编码 */
    private String roleCode;
}
