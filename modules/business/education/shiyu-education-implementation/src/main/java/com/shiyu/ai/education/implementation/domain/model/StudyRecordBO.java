package com.shiyu.ai.education.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表示 Study Record 领域对象的业务状态和属性。
 */
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

    /** 客户端为正式提交的练习答案生成的幂等键。 */
    private String submissionKey;

    /** 已提交答案的快照。 */
    private String selectedAnswer;

    /** 用于幂等重试的正确答案快照。 */
    private String correctAnswer;

    /** 用于幂等重试的解析快照。 */
    private String answerAnalysis;

    /** 批改答案时记录的题目能力维度。 */
    private String abilityDimension;

    /** 标记可信学习记录是否已计入能力画像。 */
    private Boolean analysisApplied;

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
