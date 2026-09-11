package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.ExamQuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

@Data
@Table("edu_exam_question")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ExamQuestionBO.class, reverseConvertGenerate = true)
public class ExamQuestionDO extends TenantEntity {

    @Serial private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long examId;
    private Long sectionId;
    private Long questionId;
    private Integer orderNo;
    private BigDecimal score;
}
