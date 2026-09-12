package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code KnowledgeSpaceBO} 是知识模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeSpaceBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
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
