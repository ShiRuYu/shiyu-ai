package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.education.domain.model.SubjectBO;

@AutoMapper(target = SubjectBO.class)
public record SubjectResponse(
        Long id,
        String code,
        String name,
        String gradeLevel,
        String icon,
        Integer sortOrder
) {}
