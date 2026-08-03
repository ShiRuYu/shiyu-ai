package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * StudyPlan 业务对象
 */
@Data
public class StudyPlanBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long studentId;

    private Long targetKnowledgeId;

    private String name;

    private Integer status;

    private String statusDesc;

    private LocalDate startDate;

    private LocalDate endDate;
}
