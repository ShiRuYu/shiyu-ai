package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表示 知识 Difficulty Scale 对应的持久化数据对象及其数据库字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_difficulty_scale")
@AutoMapper(target = KnowledgeDifficultyScaleBO.class, reverseConvertGenerate = true)
public class KnowledgeDifficultyScaleDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * 级别数量，表示当前对象中的对应属性。
     */
    private Integer levelCount;
}
