package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.StudyPlanDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * StudyPlan 业务对象
 */
@AutoMapper(target = StudyPlanDO.class, reverseConvertGenerate = true)
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
