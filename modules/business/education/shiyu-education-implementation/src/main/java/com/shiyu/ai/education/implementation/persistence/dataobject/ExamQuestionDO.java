package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.ExamQuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * {@code ExamQuestionDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("edu_exam_question")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ExamQuestionBO.class, reverseConvertGenerate = true)
public class ExamQuestionDO extends TenantEntity {

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
     * 考试标识，表示当前对象中的对应属性。
     */
    private Long examId;
    /**
     * 小节标识，表示当前对象中的对应属性。
     */
    private Long sectionId;
    /**
     * 题目标识，表示当前对象中的对应属性。
     */
    private Long questionId;
    /**
     * 序号，表示当前对象中的对应属性。
     */
    private Integer orderNo;
    /**
     * 分数，表示当前对象中的对应属性。
     */
    private BigDecimal score;
}
