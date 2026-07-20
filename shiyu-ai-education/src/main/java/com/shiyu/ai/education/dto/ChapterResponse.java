package com.shiyu.ai.education.dto;

import java.util.List;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.education.bo.ChapterBO;

@AutoMapper(target = ChapterBO.class)
public record ChapterResponse(
        Long id,
        Long textbookId,
        Long parentId,
        String name,
        Integer chapterOrder,
        List<ChapterResponse> children
) {}
