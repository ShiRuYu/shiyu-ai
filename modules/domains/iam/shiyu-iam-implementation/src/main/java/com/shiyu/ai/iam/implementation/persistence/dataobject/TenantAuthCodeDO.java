package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.iam.implementation.domain.model.TenantAuthCodeBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

@Data
@Table("auth_tenant_auth_code")
@AutoMapper(target = TenantAuthCodeBO.class, reverseConvertGenerate = true)
public class TenantAuthCodeDO {
    private Long tenantId;
    private Long authCodeId;
    private Integer status;
}
