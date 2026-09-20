package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ChapterBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.util.List;

/**
 * 封装 章节 相关的不可变数据及其字段约束。
 */
@AutoMapper(target = ChapterBO.class)
public record ChapterResponse(
        Long id,
        Long textbookId,
        Long parentId,
        String name,
        Integer chapterOrder,
        List<ChapterResponse> children) {}
