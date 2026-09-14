package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** StudyRecord 业务对象 */
@Data
public class StudyRecordBO implements Serializable {

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
     * recordType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String recordType;

    /**
     * 题目标识，表示当前对象中的对应属性。
     */
    private Long questionId;

    /**
     * 分数，表示当前对象中的对应属性。
     */
    private Double score;

    /**
     * accuracy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double accuracy;

    /**
     * durationSec 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer durationSec;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
