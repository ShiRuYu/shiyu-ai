package com.shiyu.ai.iam.implementation.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/** 租户上下文 VO */
@Data
@Builder
public class TenantContextVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 租户名称 */
    private String tenantName;

    /** 角色编码 */
    private String roleCode;
}
