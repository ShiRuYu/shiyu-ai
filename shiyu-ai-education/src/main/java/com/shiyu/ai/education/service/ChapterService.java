package com.shiyu.ai.education.service;

import com.shiyu.ai.education.dto.ChapterResponse;

import java.util.List;

public interface ChapterService {

    ChapterResponse getById(Long id);

    List<ChapterResponse> listByTextbookId(Long textbookId);

    List<ChapterResponse> listRootChapters(Long textbookId);

    List<ChapterResponse> listByParentId(Long parentId);
}
