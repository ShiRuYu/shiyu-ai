package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("tenant_quota")
public class TenantQuotaDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long tenantId;
    private Long maxAgentCount;
    private Long maxTokenPerDay;
    private Long maxStorageMb;
    private Long maxUserCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
