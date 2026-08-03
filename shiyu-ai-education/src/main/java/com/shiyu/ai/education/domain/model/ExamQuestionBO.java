package com.shiyu.ai.education.domain.model;
import lombok.Data;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamQuestionBO extends TenantModel {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;

    private Long examId;
    private Long sectionId;
    private Long questionId;
    private Integer orderNo;
    private BigDecimal score;
}
