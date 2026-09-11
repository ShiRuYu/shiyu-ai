package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ExamBO;

import io.github.linpeilie.annotations.AutoMapper;

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
        Integer status) {}
