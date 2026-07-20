package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.education.bo.CourseBO;

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
        Integer status
) {}
