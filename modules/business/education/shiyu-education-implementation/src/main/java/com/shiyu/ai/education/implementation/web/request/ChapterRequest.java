package com.shiyu.ai.education.implementation.web.request;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code ChapterRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class ChapterRequest implements Serializable {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * textbookId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "教材ID不能为空")
    private Long textbookId;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "章节名称不能为空")
    private String name;

    /**
     * 父级标识，表示当前对象中的对应属性。
     */
    private Long parentId;

    /**
     * 章节顺序，表示当前对象中的对应属性。
     */
    @JsonAlias("sort")
    private Integer chapterOrder;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
}
