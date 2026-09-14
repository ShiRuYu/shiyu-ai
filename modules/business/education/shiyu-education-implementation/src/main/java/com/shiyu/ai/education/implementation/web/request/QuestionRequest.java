package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code QuestionRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class QuestionRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 标题，表示当前对象中的对应属性。
     */
    @NotBlank(message = "题目标题不能为空")
    private String title;

    /**
     * 类型，表示当前对象中的对应属性。
     */
    @NotBlank(message = "题目类型不能为空")
    private String type;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    @NotBlank(message = "科目编码不能为空")
    private String subjectCode;

    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "年级不能为空")
    private Integer grade;

    /**
     * difficulty 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "难度不能为空")
    private Integer difficulty;

    /**
     * abilityDimension 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String abilityDimension;
    /**
     * 选项，表示当前对象中的对应属性。
     */
    private String options;

    /**
     * answer 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "答案不能为空")
    private String answer;

    /**
     * analysis 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String analysis;
    /**
     * tags 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String tags;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
