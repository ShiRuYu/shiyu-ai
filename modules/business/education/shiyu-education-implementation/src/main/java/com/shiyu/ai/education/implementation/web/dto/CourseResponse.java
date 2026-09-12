package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.CourseBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * {@code CourseResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param description 描述，表示该记录组件承载的数据。
 * @param subjectCode subjectCode 属性，表示该记录组件承载的数据。
 * @param grade grade 属性，表示该记录组件承载的数据。
 * @param textbookId textbookId 属性，表示该记录组件承载的数据。
 * @param teacherId teacherId 属性，表示该记录组件承载的数据。
 * @param coverUrl coverUrl 属性，表示该记录组件承载的数据。
 * @param totalHours totalHours 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 */
@AutoMapper(target = CourseBO.class)
public record CourseResponse(
        Long id,
        String name,
        String description,
        String subjectCode,
        Integer grade,
        Long textbookId,
        Long teacherId,
        String coverUrl,
        Integer totalHours,
        Integer status) {}
