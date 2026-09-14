package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ChapterBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.util.List;

/**
 * {@code ChapterResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param textbookId textbookId 属性，表示该记录组件承载的数据。
 * @param parentId parentId 属性，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param chapterOrder chapterOrder 属性，表示该记录组件承载的数据。
 * @param children children 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = ChapterBO.class)
public record ChapterResponse(
        Long id,
        Long textbookId,
        Long parentId,
        String name,
        Integer chapterOrder,
        List<ChapterResponse> children) {}
