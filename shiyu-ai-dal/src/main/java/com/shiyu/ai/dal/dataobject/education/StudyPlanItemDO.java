package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("edu_study_plan_item")
public class StudyPlanItemDO implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long planId;
    private Long knowledgeId;
    private LocalDate planDate;
    private Integer orderNo;
    private String status;
    private LocalDateTime completedAt;
}
