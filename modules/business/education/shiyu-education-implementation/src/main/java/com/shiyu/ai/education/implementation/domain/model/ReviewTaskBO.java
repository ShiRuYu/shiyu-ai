package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** ReviewTask 业务对象 */
@Data
public class ReviewTaskBO implements Serializable {

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
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long knowledgeId;

    /**
     * 题目标识，表示当前对象中的对应属性。
     */
    private Long questionId;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;

    /**
     * statusDesc 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String statusDesc;

    /**
     * reviewDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate reviewDate;

    /**
     * reviewRound 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer reviewRound;

    /**
     * 结果分数，表示当前对象中的对应属性。
     */
    private Double resultScore;

    /**
     * completedAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime completedAt;
}
