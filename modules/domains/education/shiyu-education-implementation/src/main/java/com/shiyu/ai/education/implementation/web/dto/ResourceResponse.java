package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ResourceBO;

import io.github.linpeilie.annotations.AutoMapper;

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
        Long viewCount) {}
