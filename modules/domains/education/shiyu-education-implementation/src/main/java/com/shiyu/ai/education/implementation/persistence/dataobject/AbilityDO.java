package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.AbilityBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_ability")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AbilityBO.class, reverseConvertGenerate = true)
public class AbilityDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long knowledgeId;
    private Double remember;
    private Double understand;
    private Double apply;
    private Double analyze;
    private Double evaluate;
    private Double createScore;
    private Double overallMastery;
    private LocalDateTime lastUpdate;
}

