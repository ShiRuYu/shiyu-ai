package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("auth_tenant_auth_code")
public class TenantAuthCodeDO {
    private Long tenantId;
    private Long authCodeId;
    private Integer status;
}
