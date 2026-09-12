package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.iam.implementation.domain.model.TenantMenuBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

/**
 * {@code TenantMenuDO} 是平台模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("auth_tenant_menu")
@AutoMapper(target = TenantMenuBO.class, reverseConvertGenerate = true)
public class TenantMenuDO {
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
