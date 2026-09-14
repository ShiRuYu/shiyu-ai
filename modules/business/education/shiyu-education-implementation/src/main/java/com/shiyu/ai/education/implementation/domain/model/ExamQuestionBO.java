package com.shiyu.ai.education.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * {@code ExamQuestionBO} 是教育模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExamQuestionBO extends TenantModel {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
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
