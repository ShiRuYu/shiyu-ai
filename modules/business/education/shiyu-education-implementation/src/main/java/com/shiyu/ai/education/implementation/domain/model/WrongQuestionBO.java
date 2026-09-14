package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** WrongQuestion 业务对象 */
@Data
public class WrongQuestionBO implements Serializable {

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
     * 题目标识，表示当前对象中的对应属性。
     */
    private Long questionId;

    /**
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long knowledgeId;

    /**
     * studentAnswer 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String studentAnswer;

    /**
     * correctTimes 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer correctTimes;

    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间，表示当前对象中的对应属性。
     */
    private LocalDateTime updatedAt;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
