package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.SubjectBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * {@code SubjectResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param code 编码，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param gradeLevel gradeLevel 属性，表示该记录组件承载的数据。
 * @param icon icon 属性，表示该记录组件承载的数据。
 * @param sortOrder sortOrder 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = SubjectBO.class)
public record SubjectResponse(
        Long id, String code, String name, String gradeLevel, String icon, Integer sortOrder) {}
