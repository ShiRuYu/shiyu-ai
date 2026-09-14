package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code SubjectRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class SubjectRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    @NotBlank(message = "科目编码不能为空")
    private String code;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "科目名称不能为空")
    private String name;

    /**
     * gradeLevel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String gradeLevel;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * icon 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String icon;
    /**
     * sortOrder 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer sortOrder;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
