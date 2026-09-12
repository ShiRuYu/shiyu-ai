package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Ability 业务对象 */
@Data
public class AbilityBO implements Serializable {

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
     * remember 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double remember;

    /**
     * understand 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double understand;

    /**
     * apply 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double apply;

    /**
     * analyze 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double analyze;

    /**
     * evaluate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double evaluate;

    /**
     * createScore 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double createScore;

    /**
     * overallMastery 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double overallMastery;

    /**
     * lastUpdate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime lastUpdate;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
