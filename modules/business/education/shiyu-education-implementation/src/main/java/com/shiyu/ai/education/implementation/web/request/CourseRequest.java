package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code CourseRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class CourseRequest implements Serializable {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "课程名称不能为空")
    private String name;

    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    private String subjectCode;
    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer grade;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * coverUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String coverUrl;
    /**
     * textbookId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long textbookId;
    /**
     * 教师标识，表示当前对象中的对应属性。
     */
    private Long teacherId;
    /**
     * totalHours 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer totalHours;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
