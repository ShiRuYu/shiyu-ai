package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.CourseKnowledgeBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code CourseKnowledgeDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("edu_course_knowledge")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CourseKnowledgeBO.class, reverseConvertGenerate = true)
public class CourseKnowledgeDO extends TenantEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 课程标识，表示当前对象中的对应属性。
     */
    private Long courseId;
    /**
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long knowledgeId;
    /**
     * 小节标识，表示当前对象中的对应属性。
     */
    private Long sectionId;
    /**
     * sortOrder 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer sortOrder;
}
