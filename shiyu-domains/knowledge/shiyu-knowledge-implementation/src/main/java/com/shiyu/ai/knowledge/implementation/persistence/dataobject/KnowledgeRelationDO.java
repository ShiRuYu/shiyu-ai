package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.shiyu.ai.knowledge.domain.model.KnowledgeRelationBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table(value = "knowledge_relation")
@AutoMapper(target = KnowledgeRelationBO.class, reverseConvertGenerate = true)
public class KnowledgeRelationDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long spaceId;

    private Long sourceId;

    private Long targetId;

    private String relationType;

    private Double weight;
}

