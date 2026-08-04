package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.shiyu.ai.education.domain.model.StudyPlanBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 学习计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_study_plan")
@AutoMapper(target = StudyPlanBO.class, reverseConvertGenerate = true)
public class StudyPlanDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long targetKnowledgeId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
