package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 复习任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_review_task")
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
