package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Question 业务对象 */
@Data
public class QuestionBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    /**
     * 类型，表示当前对象中的对应属性。
     */
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
     * abilityDimension 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String abilityDimension;

    /**
     * 标题，表示当前对象中的对应属性。
     */
    private String title;

    /**
     * 选项，表示当前对象中的对应属性。
     */
    private String options;

    /**
     * answer 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String answer;

    /**
     * analysis 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String analysis;

    /**
     * 来源，表示当前对象中的对应属性。
     */
    private String source;

    /**
     * tags 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String tags;

    /**
     * usedCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long usedCount;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}
