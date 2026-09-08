package com.shiyu.ai.education.service;

import com.shiyu.ai.education.dto.ChapterResponse;
import com.shiyu.ai.education.request.ChapterRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

public interface ChapterService {

    ChapterResponse getById(ActorContext actor, Long id);

    List<ChapterResponse> listByTextbookId(ActorContext actor, Long textbookId);

    List<ChapterResponse> listRootChapters(ActorContext actor, Long textbookId);

    List<ChapterResponse> listByParentId(ActorContext actor, Long parentId);

    ChapterResponse create(ActorContext actor, ChapterRequest request);

    void update(ActorContext actor, Long id, ChapterRequest request);

    void delete(ActorContext actor, Long id);

    List<Long> listKnowledgeIds(ActorContext actor, Long chapterId);

    void replaceKnowledgeIds(ActorContext actor, Long chapterId, List<Long> knowledgeIds);
}
