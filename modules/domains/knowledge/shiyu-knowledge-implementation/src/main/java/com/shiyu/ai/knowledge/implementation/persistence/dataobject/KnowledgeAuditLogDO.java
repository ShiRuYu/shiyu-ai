package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeAuditLogBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_audit_log")
@AutoMapper(target = KnowledgeAuditLogBO.class, reverseConvertGenerate = true)
public class KnowledgeAuditLogDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long spaceId;
    private String resourceType;
    private Long resourceId;
    private String action;
    private String detailJson;
}
