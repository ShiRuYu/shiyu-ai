package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("ability")
public class AbilityDO implements Serializable {

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
