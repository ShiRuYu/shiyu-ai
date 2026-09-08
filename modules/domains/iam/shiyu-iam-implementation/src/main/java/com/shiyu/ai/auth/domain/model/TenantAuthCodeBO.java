package com.shiyu.ai.auth.domain.model;
import lombok.Data;

@Data
public class TenantAuthCodeBO {
    private Long tenantId;
    private Long authCodeId;
    private Integer status;
}
