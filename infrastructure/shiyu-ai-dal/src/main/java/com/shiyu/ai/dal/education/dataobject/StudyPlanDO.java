package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学习计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_study_plan")
public class StudyPlanDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long targetKnowledgeId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
