package com.shiyu.ai.education.implementation.web.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;

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
        String className
) {}

