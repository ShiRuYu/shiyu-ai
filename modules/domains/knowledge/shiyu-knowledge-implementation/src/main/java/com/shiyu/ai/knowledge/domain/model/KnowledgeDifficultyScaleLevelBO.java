package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeDifficultyScaleLevelBO extends TenantModel {
    private Long id;
    private Long scaleId;
    private Integer level;
    private String label;
    private String description;
}
