package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.education.bo.ExamBO;

@AutoMapper(target = ExamBO.class)
public record ExamResponse(
        Long id,
        String name,
        String type,
        String subjectCode,
        Integer grade,
        Integer durationMin,
        Integer totalScore,
        Integer status
) {}
