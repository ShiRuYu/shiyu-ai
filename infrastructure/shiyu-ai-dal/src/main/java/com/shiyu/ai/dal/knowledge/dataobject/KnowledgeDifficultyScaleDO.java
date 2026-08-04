package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_difficulty_scale")
@AutoMapper(target = KnowledgeDifficultyScaleBO.class, reverseConvertGenerate = true)
public class KnowledgeDifficultyScaleDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer levelCount;
}
