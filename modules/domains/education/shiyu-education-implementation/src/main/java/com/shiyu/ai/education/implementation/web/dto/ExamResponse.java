package com.shiyu.ai.education.implementation.web.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.education.implementation.domain.model.ExamBO;

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
        Integer status
) {}

