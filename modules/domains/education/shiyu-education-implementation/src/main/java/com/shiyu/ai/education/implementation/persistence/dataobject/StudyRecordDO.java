package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.StudyRecordBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_study_record")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = StudyRecordBO.class, reverseConvertGenerate = true)
public class StudyRecordDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long knowledgeId;
    private String recordType;
    private Long questionId;
    private Double score;
    private Double accuracy;
    private Integer durationSec;
}

