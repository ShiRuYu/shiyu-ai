package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import com.mybatisflex.annotation.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.auth.domain.model.TenantQuotaBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 租户配额
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("auth_tenant_quota")
@AutoMapper(target = TenantQuotaBO.class, reverseConvertGenerate = true)
public class TenantQuotaDO extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column(tenantId = true)
    private Long tenantId;

    private Long maxAgentCount;
    private Long maxTokenPerDay;
    private Long maxStorageMb;
    private Long maxUserCount;

}
