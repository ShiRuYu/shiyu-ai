package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.CourseBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_course")
@AutoMapper(target = CourseBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class CourseDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;
    private String description;
    private String subjectCode;
    private Integer grade;
    private Long textbookId;
    private Long teacherId;
    private String coverUrl;
    private Integer totalHours;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

