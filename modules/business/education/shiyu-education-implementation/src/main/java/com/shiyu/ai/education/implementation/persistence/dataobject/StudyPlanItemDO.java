package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.StudyPlanItemBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 学习计划项 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_study_plan_item")
@AutoMapper(target = StudyPlanItemBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class StudyPlanItemDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * planId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long planId;

    /**
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long knowledgeId;

    /**
     * planDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate planDate;

    /**
     * 序号，表示当前对象中的对应属性。
     */
    private Integer orderNo;

    /**
     * completedAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime completedAt;
}
