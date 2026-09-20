package com.shiyu.ai.education.implementation.web.request;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 封装 Study Plan 操作所需的请求条件和输入数据。
 */
@Data
public class StudyPlanRequest implements Serializable {

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
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * startDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate startDate;
    /**
     * endDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate endDate;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private String status;
}
