package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentRelationBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code KnowledgeDocumentRelationDO} 是知识模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_document_relation")
@AutoMapper(target = KnowledgeDocumentRelationBO.class, reverseConvertGenerate = true)
public class KnowledgeDocumentRelationDO extends TenantEntity {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;
    /**
     * sourceDocumentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long sourceDocumentId;
    /**
     * targetDocumentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long targetDocumentId;
    /**
     * relationType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String relationType;
}
