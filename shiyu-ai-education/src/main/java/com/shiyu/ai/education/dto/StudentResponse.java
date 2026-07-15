package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.bo.education.StudentBO;

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
