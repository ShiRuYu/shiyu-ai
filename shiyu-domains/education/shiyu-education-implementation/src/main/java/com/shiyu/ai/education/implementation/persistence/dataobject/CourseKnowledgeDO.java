package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import com.shiyu.ai.education.domain.model.CourseKnowledgeBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_course_knowledge")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CourseKnowledgeBO.class, reverseConvertGenerate = true)
public class CourseKnowledgeDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long courseId;
    private Long knowledgeId;
    private Long sectionId;
    private Integer sortOrder;
}

