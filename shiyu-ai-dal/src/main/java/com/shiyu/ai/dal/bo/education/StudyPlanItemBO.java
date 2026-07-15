package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.StudyPlanItemDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * StudyPlanItem 业务对象
 */
@AutoMapper(target = StudyPlanItemDO.class, reverseConvertGenerate = true)
@Data
public class StudyPlanItemBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long planId;

    private Long knowledgeId;

    private LocalDate planDate;

    private Integer orderNo;

    private String status;

    private LocalDateTime completedAt;

}
