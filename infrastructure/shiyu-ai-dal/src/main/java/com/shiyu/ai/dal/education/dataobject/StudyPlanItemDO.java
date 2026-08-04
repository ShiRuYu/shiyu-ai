package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.StudyPlanItemBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 学习计划项
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_study_plan_item")
@AutoMapper(target = StudyPlanItemBO.class, reverseConvertGenerate = true)
public class StudyPlanItemDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long planId;

    private Long knowledgeId;

    private LocalDate planDate;

    private Integer orderNo;

    private LocalDateTime completedAt;
}
