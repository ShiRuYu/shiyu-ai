package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeRelationBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code KnowledgeRelationDO} 是知识模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table(value = "knowledge_relation")
@AutoMapper(target = KnowledgeRelationBO.class, reverseConvertGenerate = true)
public class KnowledgeRelationDO extends TenantEntity {

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
     * 来源标识，表示当前对象中的对应属性。
     */
    private Long sourceId;

    /**
     * 目标标识，表示当前对象中的对应属性。
     */
    private Long targetId;

    /**
     * relationType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String relationType;

    /**
     * 权重，表示当前对象中的对应属性。
     */
    private Double weight;
}
