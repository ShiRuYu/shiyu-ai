package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.ExamSectionBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_exam_section")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ExamSectionBO.class, reverseConvertGenerate = true)
public class ExamSectionDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long examId;
    private String name;
    private Integer orderNo;
    private BigDecimal scorePerQ;
    private LocalDateTime createdAt;
}
