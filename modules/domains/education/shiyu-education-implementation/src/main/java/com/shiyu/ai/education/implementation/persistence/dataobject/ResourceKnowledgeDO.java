package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.ResourceKnowledgeBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@Table("edu_resource_knowledge")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ResourceKnowledgeBO.class, reverseConvertGenerate = true)
public class ResourceKnowledgeDO extends TenantEntity {

    @Serial private static final long serialVersionUID = 1L;

    private Long resourceId;
    private Long knowledgeId;
    private Integer sortOrder;
}
