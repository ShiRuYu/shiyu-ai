package com.shiyu.ai.auth.domain.model;
import lombok.Data;

/**
 * 租户配额业务对象
 */
@Data
public class TenantQuotaBO {

    private Long id;
    private Long tenantId;
    private Long maxAgentCount;
    private Long maxTokenPerDay;
    private Long maxStorageMb;
    private Long maxUserCount;
    private Integer status;
    private String statusDesc;
}
