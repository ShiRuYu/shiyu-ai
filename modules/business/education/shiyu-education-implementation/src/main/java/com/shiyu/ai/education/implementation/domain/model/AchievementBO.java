package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Achievement 业务对象 */
@Data
public class AchievementBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 学生标识，表示当前对象中的对应属性。
     */
    private Long studentId;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * icon 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String icon;

    /**
     * earnedAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime earnedAt;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
