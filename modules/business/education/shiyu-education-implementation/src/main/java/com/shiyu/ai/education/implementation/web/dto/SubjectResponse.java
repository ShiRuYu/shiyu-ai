package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.SubjectBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * 封装 学科 相关的不可变数据及其字段约束。
 */
@AutoMapper(target = SubjectBO.class)
public record SubjectResponse(
        Long id, String code, String name, String gradeLevel, String icon, Integer sortOrder) {}
