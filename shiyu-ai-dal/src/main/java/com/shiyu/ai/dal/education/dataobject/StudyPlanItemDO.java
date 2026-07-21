package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习计划项
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_study_plan_item")
public class StudyPlanItemDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long planId;

    private Long knowledgeId;

    private LocalDate planDate;

    private Integer orderNo;

    private LocalDateTime completedAt;
}
