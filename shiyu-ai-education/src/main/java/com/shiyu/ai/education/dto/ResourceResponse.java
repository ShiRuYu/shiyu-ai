package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.bo.education.ResourceBO;

@AutoMapper(target = ResourceBO.class)
public record ResourceResponse(
        Long id,
        String name,
        String type,
        String url,
        String subjectCode,
        Integer grade,
        Integer difficulty,
        String coverUrl,
        String description,
        Long viewCount
) {}
