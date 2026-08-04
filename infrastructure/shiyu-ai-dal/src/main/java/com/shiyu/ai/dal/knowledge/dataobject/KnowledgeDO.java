package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "knowledge_base")
@AutoMapper(target = KnowledgeBO.class, reverseConvertGenerate = true)
public class KnowledgeDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long spaceId;

    private String code;

    private String name;

    private String description;

    private Integer difficulty;

    private Integer difficultyLevel;

    private String category;

    private String tags;

}
