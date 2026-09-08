package com.shiyu.ai.auth.domain.model;
import lombok.Data;

@Data
public class TenantMenuBO {
    private Long tenantId;
    private Long menuId;
    private Integer status;
}
