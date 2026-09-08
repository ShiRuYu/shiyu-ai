package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * StudyPlanItem 业务对象
 */
@Data
public class StudyPlanItemBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long planId;

    private Long knowledgeId;

    private LocalDate planDate;

    private Integer orderNo;

    private Integer status;

    private String statusDesc;

    private LocalDateTime completedAt;
}
