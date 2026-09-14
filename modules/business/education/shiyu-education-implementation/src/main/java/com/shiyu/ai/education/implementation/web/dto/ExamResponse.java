package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ExamBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * {@code ExamResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param type 类型，表示该记录组件承载的数据。
 * @param subjectCode subjectCode 属性，表示该记录组件承载的数据。
 * @param grade grade 属性，表示该记录组件承载的数据。
 * @param teacherId teacherId 属性，表示该记录组件承载的数据。
 * @param durationMin durationMin 属性，表示该记录组件承载的数据。
 * @param totalScore totalScore 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 */
@AutoMapper(target = ExamBO.class)
public record ExamResponse(
        Long id,
        String name,
        String type,
        String subjectCode,
        Integer grade,
        Long teacherId,
        Integer durationMin,
        Integer totalScore,
        Integer status) {}
