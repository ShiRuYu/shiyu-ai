package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.CourseSectionBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_course_section")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CourseSectionBO.class, reverseConvertGenerate = true)
public class CourseSectionDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long chapterId;
    private String name;
    private Integer orderNo;
    private String contentUrl;
    private String videoUrl;
    private Integer durationMin;
    private LocalDateTime createdAt;
}
