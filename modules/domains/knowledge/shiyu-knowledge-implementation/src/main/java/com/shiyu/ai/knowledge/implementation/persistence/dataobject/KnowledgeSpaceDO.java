package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_space")
@AutoMapper(target = KnowledgeSpaceBO.class, reverseConvertGenerate = true)
public class KnowledgeSpaceDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private String code;
    /** Business domain of the space, for example GENERAL, ENTERPRISE or EDUCATION. */
    private String domainCode;
    private String name;
    private String description;
    private String accessMode;
    private String reviewMode;
    private String bindingMode;
    private Long difficultyScaleId;
    private String embeddingProfile;
    private String rerankProfile;
    private String chunkStrategy;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private Long activeIndexVersion;
}

