package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.AbilityBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@Table("edu_ability")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AbilityBO.class, reverseConvertGenerate = true)
public class AbilityDO extends TenantEntity {

    @Serial private static final long serialVersionUID = 1L;

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
