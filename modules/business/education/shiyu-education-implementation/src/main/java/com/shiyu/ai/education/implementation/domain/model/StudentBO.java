package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Student 业务对象 */
@Data
public class StudentBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;

    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;

    /**
     * studentNo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String studentNo;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * gender 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer gender;

    /**
     * birthDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate birthDate;

    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer grade;

    /**
     * gradeLevel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String gradeLevel;

    /**
     * school 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String school;

    /**
     * className 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String className;

    /**
     * 父级标识，表示当前对象中的对应属性。
     */
    private Long parentId;

    /**
     * learningStyle 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String learningStyle;

    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间，表示当前对象中的对应属性。
     */
    private LocalDateTime updatedAt;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
