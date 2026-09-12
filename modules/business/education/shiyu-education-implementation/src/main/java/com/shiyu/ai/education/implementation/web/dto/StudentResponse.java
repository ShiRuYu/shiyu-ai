package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.StudentBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * {@code StudentResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param userId 用户标识，表示该记录组件承载的数据。
 * @param studentNo studentNo 属性，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param gender gender 属性，表示该记录组件承载的数据。
 * @param grade grade 属性，表示该记录组件承载的数据。
 * @param gradeLevel gradeLevel 属性，表示该记录组件承载的数据。
 * @param school school 属性，表示该记录组件承载的数据。
 * @param className className 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = StudentBO.class)
public record StudentResponse(
        Long id,
        Long userId,
        String studentNo,
        String name,
        Integer gender,
        Integer grade,
        String gradeLevel,
        String school,
        String className) {}
