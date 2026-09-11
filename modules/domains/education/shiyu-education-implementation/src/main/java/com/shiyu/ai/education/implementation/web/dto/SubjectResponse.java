package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.SubjectBO;

import io.github.linpeilie.annotations.AutoMapper;

@AutoMapper(target = SubjectBO.class)
public record SubjectResponse(
        Long id, String code, String name, String gradeLevel, String icon, Integer sortOrder) {}
