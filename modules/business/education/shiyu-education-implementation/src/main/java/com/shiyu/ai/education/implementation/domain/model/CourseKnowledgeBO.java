package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** CourseKnowledge 业务对象 */
@Data
public class CourseKnowledgeBO implements Serializable {

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

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
