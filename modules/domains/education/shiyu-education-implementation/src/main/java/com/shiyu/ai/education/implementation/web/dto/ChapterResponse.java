package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ChapterBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.util.List;

@AutoMapper(target = ChapterBO.class)
public record ChapterResponse(
        Long id,
        Long textbookId,
        Long parentId,
        String name,
        Integer chapterOrder,
        List<ChapterResponse> children) {}
