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
 * {@code StudyRecordDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
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
