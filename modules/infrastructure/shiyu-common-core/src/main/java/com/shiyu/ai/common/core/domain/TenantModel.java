package com.shiyu.ai.common.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 实现 租户 模型 所属领域的业务规则和状态变化。
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
