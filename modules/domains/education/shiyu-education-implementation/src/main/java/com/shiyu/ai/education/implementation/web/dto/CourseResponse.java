package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.CourseBO;

import io.github.linpeilie.annotations.AutoMapper;

@AutoMapper(target = CourseBO.class)
public record CourseResponse(
        Long id,
        String name,
        String description,
        String subjectCode,
        Integer grade,
        Long textbookId,
        Long teacherId,
        String coverUrl,
        Integer totalHours,
        Integer status) {}
