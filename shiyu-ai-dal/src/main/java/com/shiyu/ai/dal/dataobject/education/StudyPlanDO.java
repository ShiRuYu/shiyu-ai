package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Table("edu_study_plan")
public class StudyPlanDO implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long targetKnowledgeId;
    private String name;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
