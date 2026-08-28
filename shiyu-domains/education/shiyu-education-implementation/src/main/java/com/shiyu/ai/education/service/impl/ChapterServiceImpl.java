package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ChapterBO;
import com.shiyu.ai.education.port.repository.ChapterRepository;
import com.shiyu.ai.education.port.repository.KnowledgeTextbookRepository;
import com.shiyu.ai.education.domain.model.KnowledgeTextbookBO;
import com.shiyu.ai.education.dto.ChapterResponse;
import com.shiyu.ai.education.request.ChapterRequest;
import com.shiyu.ai.education.service.ChapterService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("lambda")
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final KnowledgeTextbookRepository knowledgeTextbookRepository;

    @Override
    public ChapterResponse getById(ActorContext actor, Long id) {
        ChapterBO bo = chapterRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, ChapterResponse.class);
    }

    @Override
    public List<ChapterResponse> listByTextbookId(ActorContext actor, Long textbookId) {
        List<ChapterBO> boList = chapterRepository.selectByTextbookId(requireActor(actor).tenantId(), textbookId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    @Override
    public List<ChapterResponse> listRootChapters(ActorContext actor, Long textbookId) {
        List<ChapterBO> boList = chapterRepository.selectRootChapters(requireActor(actor).tenantId(), textbookId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    @Override
    public List<ChapterResponse> listByParentId(ActorContext actor, Long parentId) {
        List<ChapterBO> boList = chapterRepository.selectByParentId(requireActor(actor).tenantId(), parentId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterResponse create(ActorContext actor, ChapterRequest request) {
        actor = requireActor(actor);
        ChapterBO chapter = new ChapterBO();
        applyRequest(chapter, request);
        chapterRepository.insert(actor.tenantId(), chapter);
        return MapstructUtils.convert(chapter, ChapterResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, Long id, ChapterRequest request) {
        actor = requireActor(actor);
        ChapterBO chapter = chapterRepository.selectById(actor.tenantId(), id);
        if (chapter == null) {
            throw new ServiceException("章节不存在: " + id);
        }
        applyRequest(chapter, request);
        chapterRepository.update(actor.tenantId(), chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ActorContext actor, Long id) {
        actor = requireActor(actor);
        if (!chapterRepository.selectByParentId(actor.tenantId(), id).isEmpty()) {
            throw new ServiceException("请先删除子章节");
        }
        knowledgeTextbookRepository.deleteByChapterId(actor.tenantId(), id);
        chapterRepository.deleteById(actor.tenantId(), id);
    }

    @Override
    public List<Long> listKnowledgeIds(ActorContext actor, Long chapterId) {
        return knowledgeTextbookRepository.selectByChapterId(requireActor(actor).tenantId(), chapterId).stream()
                .map(KnowledgeTextbookBO::getKnowledgeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceKnowledgeIds(ActorContext actor, Long chapterId, List<Long> knowledgeIds) {
        actor = requireActor(actor);
        ChapterBO chapter = chapterRepository.selectById(actor.tenantId(), chapterId);
        if (chapter == null) {
            throw new ServiceException("章节不存在: " + chapterId);
        }
        knowledgeTextbookRepository.deleteByChapterId(actor.tenantId(), chapterId);
        if (knowledgeIds == null) {
            return;
        }
        java.util.Set<Long> distinctKnowledgeIds = new java.util.LinkedHashSet<>();
        for (Long knowledgeId : knowledgeIds) {
            if (knowledgeId != null) {
                distinctKnowledgeIds.add(knowledgeId);
            }
        }
        for (Long knowledgeId : distinctKnowledgeIds) {
            KnowledgeTextbookBO relation = new KnowledgeTextbookBO();
            relation.setChapterId(chapterId);
            relation.setTextbookId(chapter.getTextbookId());
            relation.setKnowledgeId(knowledgeId);
            relation.setStatus(1);
            knowledgeTextbookRepository.insert(actor.tenantId(), relation);
        }
    }

    private static void applyRequest(ChapterBO chapter, ChapterRequest request) {
        chapter.setTextbookId(request.getTextbookId());
        chapter.setName(request.getName());
        chapter.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        chapter.setChapterOrder(request.getChapterOrder() == null ? 0 : request.getChapterOrder());
        chapter.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }

    private static ActorContext requireActor(ActorContext actor) {
        return Objects.requireNonNull(actor, "actor is required");
    }
}
