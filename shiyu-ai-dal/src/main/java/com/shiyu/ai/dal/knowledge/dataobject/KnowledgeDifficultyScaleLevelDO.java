package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_difficulty_scale_level")
public class KnowledgeDifficultyScaleLevelDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long scaleId;
    private Integer level;
    private String label;
    private String description;
}
