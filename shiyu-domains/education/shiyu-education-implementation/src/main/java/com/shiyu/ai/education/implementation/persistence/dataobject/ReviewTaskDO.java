package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 复习任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_review_task")
@AutoMapper(target = ReviewTaskBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class ReviewTaskDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long knowledgeId;

    @Column(ignore = true)
    private Long questionId;

    private LocalDate reviewDate;
    private Integer reviewRound;
    private Double resultScore;
    private LocalDateTime completedAt;
}

