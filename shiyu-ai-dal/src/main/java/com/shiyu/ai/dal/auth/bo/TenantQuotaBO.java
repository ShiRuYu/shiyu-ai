package com.shiyu.ai.dal.auth.bo;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.auth.dataobject.TenantQuotaDO;
import lombok.Data;

/**
 * 租户配额业务对象
 */
@AutoMapper(target = TenantQuotaDO.class, reverseConvertGenerate = true)
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
