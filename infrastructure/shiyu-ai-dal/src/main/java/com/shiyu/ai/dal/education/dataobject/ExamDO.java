package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.ExamBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_exam")
@AutoMapper(target = ExamBO.class, reverseConvertGenerate = true)
public class ExamDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;
    private String type;
    private String subjectCode;
    private Integer grade;
    private Integer durationMin;
    private Integer totalScore;
    private Long teacherId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
}
