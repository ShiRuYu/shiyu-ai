package com.shiyu.ai.education.domain.model;
import lombok.Data;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamSectionBO extends TenantModel {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;

    private Long examId;
    private String name;
    private Integer orderNo;
    private BigDecimal scorePerQ;
    private LocalDateTime createdAt;
}
