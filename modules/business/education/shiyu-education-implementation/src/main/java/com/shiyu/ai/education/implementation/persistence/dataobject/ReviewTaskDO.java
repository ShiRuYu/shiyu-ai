package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.ReviewTaskBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 复习任务 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_review_task")
@AutoMapper(target = ReviewTaskBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class ReviewTaskDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 学生标识，表示当前对象中的对应属性。
     */
    private Long studentId;
    /**
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long knowledgeId;

    /**
     * 题目标识，表示当前对象中的对应属性。
     */
    @Column(ignore = true)
    private Long questionId;

    /**
     * reviewDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate reviewDate;
    /**
     * reviewRound 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer reviewRound;
    /**
     * 结果分数，表示当前对象中的对应属性。
     */
    private Double resultScore;
    /**
     * completedAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime completedAt;
}
