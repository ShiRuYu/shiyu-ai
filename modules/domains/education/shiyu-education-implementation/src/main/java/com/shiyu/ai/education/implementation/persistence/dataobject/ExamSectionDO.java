package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.ExamSectionBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("edu_exam_section")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ExamSectionBO.class, reverseConvertGenerate = true)
public class ExamSectionDO extends TenantEntity {

    @Serial private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long examId;
    private String name;
    private Integer orderNo;
    private BigDecimal scorePerQ;
    private LocalDateTime createdAt;
}
