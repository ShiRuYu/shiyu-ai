package com.shiyu.ai.common.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 承载租户基础信息及其持久化字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantModel extends BaseEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
}
