package com.shiyu.ai.iam.implementation.domain.model;

import lombok.Data;

@Data
public class TenantMenuBO {
    private Long tenantId;
    private Long menuId;
    private Integer status;
}
