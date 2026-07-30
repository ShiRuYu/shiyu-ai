package com.shiyu.ai.education.service;

import com.shiyu.ai.education.dto.ChapterResponse;
import com.shiyu.ai.education.request.ChapterRequest;

import java.util.List;

public interface ChapterService {

    ChapterResponse getById(Long id);

    List<ChapterResponse> listByTextbookId(Long textbookId);

    List<ChapterResponse> listRootChapters(Long textbookId);

    List<ChapterResponse> listByParentId(Long parentId);

    ChapterResponse create(ChapterRequest request);

    void update(Long id, ChapterRequest request);

    void delete(Long id);

    List<Long> listKnowledgeIds(Long chapterId);

    void replaceKnowledgeIds(Long chapterId, List<Long> knowledgeIds);
}
