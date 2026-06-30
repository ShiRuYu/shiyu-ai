package com.shiyu.ai.education.dto;

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
