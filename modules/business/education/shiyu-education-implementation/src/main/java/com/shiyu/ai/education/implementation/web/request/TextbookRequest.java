package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code TextbookRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class TextbookRequest implements Serializable {

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
    @NotBlank(message = "教材名称不能为空")
    private String name;

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
     * 发布器，表示当前对象中的对应属性。
     */
    @NotBlank(message = "出版社不能为空")
    private String publisher;

    /**
     * author 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String author;
    /**
     * edition 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String edition;
    /**
     * isbn 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String isbn;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
