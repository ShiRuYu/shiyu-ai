package com.shiyu.ai.iam.implementation.domain.model;

import lombok.Data;

/**
 * 表示 租户 认证 Code 领域对象的业务状态和属性。
 */
@Data
public class TenantAuthCodeBO {
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
    /**
     * authCodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long authCodeId;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
