package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** StudyPlan 业务对象 */
@Data
public class StudyPlanBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 学生标识，表示当前对象中的对应属性。
     */
    private Long studentId;

    /**
     * targetKnowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long targetKnowledgeId;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;

    /**
     * statusDesc 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String statusDesc;

    /**
     * startDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate startDate;

    /**
     * endDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate endDate;
}
