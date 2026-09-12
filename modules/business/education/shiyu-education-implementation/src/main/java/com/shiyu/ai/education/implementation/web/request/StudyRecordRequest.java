package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code StudyRecordRequest} 表示教育模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class StudyRecordRequest implements Serializable {

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
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    /**
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "知识点ID不能为空")
    private Long knowledgeId;

    /**
     * recordType 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "学习记录类型不能为空")
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
