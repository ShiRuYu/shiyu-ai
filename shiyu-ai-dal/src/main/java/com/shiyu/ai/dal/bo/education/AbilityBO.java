package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.AbilityDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Ability 业务对象
 */
@AutoMapper(target = AbilityDO.class, reverseConvertGenerate = true)
@Data
public class AbilityBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;



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

    private LocalDateTime createTime;

}
