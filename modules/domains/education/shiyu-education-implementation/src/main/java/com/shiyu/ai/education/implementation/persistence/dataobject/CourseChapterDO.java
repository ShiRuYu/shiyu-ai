package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.CourseChapterBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@Table("edu_course_chapter")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CourseChapterBO.class, reverseConvertGenerate = true)
public class CourseChapterDO extends TenantEntity {

    @Serial private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long courseId;
    private String name;
    private Integer orderNo;
    private LocalDateTime createdAt;
}
