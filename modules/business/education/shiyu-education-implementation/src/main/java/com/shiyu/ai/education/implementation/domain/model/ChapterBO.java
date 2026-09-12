package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Chapter 业务对象 */
@Data
public class ChapterBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * textbookId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long textbookId;

    /**
     * 父级标识，表示当前对象中的对应属性。
     */
    private Long parentId;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 章节顺序，表示当前对象中的对应属性。
     */
    private Integer chapterOrder;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
