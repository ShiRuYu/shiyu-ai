package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 表示 Study Record 对应的持久化数据对象及其数据库字段。
 */
@Data
@Table("edu_study_record")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = StudyRecordBO.class, reverseConvertGenerate = true)
public class StudyRecordDO extends TenantEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
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
    /** 练习提交的幂等键。 */
    private String submissionKey;
    /** 已提交答案的快照。 */
    private String selectedAnswer;
    /** 正确答案快照。 */
    private String correctAnswer;
    /** 解析快照。 */
    private String answerAnalysis;
    /** 已评分题目的能力维度。 */
    private String abilityDimension;
    /** 防止重复分析的标记。 */
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
}
