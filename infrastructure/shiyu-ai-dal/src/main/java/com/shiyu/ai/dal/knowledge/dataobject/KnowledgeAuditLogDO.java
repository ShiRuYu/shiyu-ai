package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_audit_log")
public class KnowledgeAuditLogDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long spaceId;
    private String resourceType;
    private Long resourceId;
    private String action;
    private String detailJson;
}
