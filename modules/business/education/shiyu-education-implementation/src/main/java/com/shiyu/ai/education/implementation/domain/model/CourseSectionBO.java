package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** CourseSection 业务对象 */
@Data
public class CourseSectionBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 章节标识，表示当前对象中的对应属性。
     */
    private Long chapterId;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 序号，表示当前对象中的对应属性。
     */
    private Integer orderNo;

    /**
     * 内容地址，表示当前对象中的对应属性。
     */
    private String contentUrl;

    /**
     * videoUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String videoUrl;

    /**
     * durationMin 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer durationMin;

    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
