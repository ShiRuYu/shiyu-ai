package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeSpaceBO extends TenantModel {
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
