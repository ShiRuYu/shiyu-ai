package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code ResourceRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class ResourceRequest implements Serializable {

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
    @NotBlank(message = "资源名称不能为空")
    private String name;

    /**
     * 类型，表示当前对象中的对应属性。
     */
    @NotBlank(message = "资源类型不能为空")
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
     * difficulty 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer difficulty;
    /**
     * coverUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String coverUrl;

    /**
     * 地址，表示当前对象中的对应属性。
     */
    @NotBlank(message = "资源链接不能为空")
    private String url;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
