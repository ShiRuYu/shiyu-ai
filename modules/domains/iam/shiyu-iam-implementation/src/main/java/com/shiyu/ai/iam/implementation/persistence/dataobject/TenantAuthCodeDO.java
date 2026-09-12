package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.iam.implementation.domain.model.TenantAuthCodeBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

/**
 * {@code TenantAuthCodeDO} 是平台模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("auth_tenant_auth_code")
@AutoMapper(target = TenantAuthCodeBO.class, reverseConvertGenerate = true)
public class TenantAuthCodeDO {
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
