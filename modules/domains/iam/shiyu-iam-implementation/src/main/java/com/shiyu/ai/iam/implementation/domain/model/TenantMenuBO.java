package com.shiyu.ai.iam.implementation.domain.model;

import lombok.Data;

/**
 * {@code TenantMenuBO} 是模型模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
public class TenantMenuBO {
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
    /**
     * menuId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long menuId;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
