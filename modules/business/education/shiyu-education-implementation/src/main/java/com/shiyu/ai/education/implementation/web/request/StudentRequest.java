package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code StudentRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class StudentRequest implements Serializable {

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
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * studentNo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String studentNo;

    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "年级不能为空")
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
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
