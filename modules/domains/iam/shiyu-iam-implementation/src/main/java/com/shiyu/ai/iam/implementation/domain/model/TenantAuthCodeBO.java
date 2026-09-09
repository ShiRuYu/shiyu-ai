package com.shiyu.ai.iam.implementation.domain.model;
import lombok.Data;

@Data
public class TenantAuthCodeBO {
    private Long tenantId;
    private Long authCodeId;
    private Integer status;
}

