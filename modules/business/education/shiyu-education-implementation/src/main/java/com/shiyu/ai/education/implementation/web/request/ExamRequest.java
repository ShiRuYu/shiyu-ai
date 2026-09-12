package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code ExamRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class ExamRequest implements Serializable {

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
    @NotBlank(message = "考试名称不能为空")
    private String name;

    /**
     * 类型，表示当前对象中的对应属性。
     */
    @NotBlank(message = "考试类型不能为空")
    private String type;

    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    @NotBlank(message = "学科编码不能为空")
    private String subjectCode;

    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "年级不能为空")
    private Integer grade;

    /**
     * 教师标识，表示当前对象中的对应属性。
     */
    private Long teacherId;

    /**
     * durationMin 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "考试时长不能为空")
    private Integer durationMin;

    /**
     * totalScore 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "总分不能为空")
    private Integer totalScore;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
