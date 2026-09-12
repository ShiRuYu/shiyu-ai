package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Exam 业务对象 */
@Data
public class ExamBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 类型，表示当前对象中的对应属性。
     */
    private String type;

    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    private String subjectCode;

    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer grade;

    /**
     * durationMin 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer durationMin;

    /**
     * totalScore 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer totalScore;

    /**
     * 教师标识，表示当前对象中的对应属性。
     */
    private Long teacherId;

    /**
     * startTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime startTime;

    /**
     * endTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime endTime;

    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
