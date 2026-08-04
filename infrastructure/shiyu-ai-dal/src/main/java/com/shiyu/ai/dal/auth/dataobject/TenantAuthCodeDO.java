package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import com.shiyu.ai.auth.domain.model.TenantAuthCodeBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("auth_tenant_auth_code")
@AutoMapper(target = TenantAuthCodeBO.class, reverseConvertGenerate = true)
public class TenantAuthCodeDO {
    private Long tenantId;
    private Long authCodeId;
    private Integer status;
}
