package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDifficultyScaleBO extends TenantModel {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer levelCount;
}
