package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@Table("ability")
@EqualsAndHashCode(callSuper = true)
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
