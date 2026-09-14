package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code KnowledgeSpaceDO} 是知识模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_space")
@AutoMapper(target = KnowledgeSpaceBO.class, reverseConvertGenerate = true)
public class KnowledgeSpaceDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    /** 知识领域编码。 */
    private String domainCode;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * accessMode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String accessMode;
    /**
     * reviewMode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String reviewMode;
    /**
     * bindingMode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String bindingMode;
    /**
     * difficultyScaleId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long difficultyScaleId;
    /**
     * 嵌入向量配置档案，表示当前对象中的对应属性。
     */
    private String embeddingProfile;
    /**
     * rerankProfile 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String rerankProfile;
    /**
     * chunkStrategy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String chunkStrategy;
    /**
     * chunkSize 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer chunkSize;
    /**
     * chunkOverlap 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer chunkOverlap;
    /**
     * activeIndexVersion 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long activeIndexVersion;
}
