package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code WrongQuestionDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_wrong_question")
@AutoMapper(target = WrongQuestionBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class WrongQuestionDO extends TenantEntity {

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
}
