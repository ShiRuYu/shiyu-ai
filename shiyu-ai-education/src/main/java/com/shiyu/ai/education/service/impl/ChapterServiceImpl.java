package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.ChapterBO;
import com.shiyu.ai.dal.education.repository.ChapterRepository;
import com.shiyu.ai.dal.education.repository.KnowledgeTextbookRepository;
import com.shiyu.ai.dal.education.bo.KnowledgeTextbookBO;
import com.shiyu.ai.education.dto.ChapterResponse;
import com.shiyu.ai.education.request.ChapterRequest;
import com.shiyu.ai.education.service.ChapterService;
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
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final KnowledgeTextbookRepository knowledgeTextbookRepository;

    @Override
    public ChapterResponse getById(Long id) {
        ChapterBO bo = chapterRepository.selectById(id);
        return MapstructUtils.convert(bo, ChapterResponse.class);
    }

    @Override
    public List<ChapterResponse> listByTextbookId(Long textbookId) {
        List<ChapterBO> boList = chapterRepository.selectByTextbookId(textbookId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    @Override
    public List<ChapterResponse> listRootChapters(Long textbookId) {
        List<ChapterBO> boList = chapterRepository.selectRootChapters(textbookId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    @Override
    public List<ChapterResponse> listByParentId(Long parentId) {
        List<ChapterBO> boList = chapterRepository.selectByParentId(parentId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterResponse create(ChapterRequest request) {
        ChapterBO chapter = new ChapterBO();
        applyRequest(chapter, request);
        chapterRepository.insert(chapter);
        return MapstructUtils.convert(chapter, ChapterResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ChapterRequest request) {
        ChapterBO chapter = chapterRepository.selectById(id);
        if (chapter == null) {
            throw new ServiceException("章节不存在: " + id);
        }
        applyRequest(chapter, request);
        chapterRepository.update(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!chapterRepository.selectByParentId(id).isEmpty()) {
            throw new ServiceException("请先删除子章节");
        }
        knowledgeTextbookRepository.deleteByChapterId(id);
        chapterRepository.deleteById(id);
    }

    @Override
    public List<Long> listKnowledgeIds(Long chapterId) {
        return knowledgeTextbookRepository.selectByChapterId(chapterId).stream()
                .map(KnowledgeTextbookBO::getKnowledgeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceKnowledgeIds(Long chapterId, List<Long> knowledgeIds) {
        ChapterBO chapter = chapterRepository.selectById(chapterId);
        if (chapter == null) {
            throw new ServiceException("章节不存在: " + chapterId);
        }
        knowledgeTextbookRepository.deleteByChapterId(chapterId);
        if (knowledgeIds == null) {
            return;
        }
        knowledgeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(knowledgeId -> {
                    KnowledgeTextbookBO relation = new KnowledgeTextbookBO();
                    relation.setChapterId(chapterId);
                    relation.setTextbookId(chapter.getTextbookId());
                    relation.setKnowledgeId(knowledgeId);
                    relation.setStatus(1);
                    knowledgeTextbookRepository.insert(relation);
                });
    }

    private static void applyRequest(ChapterBO chapter, ChapterRequest request) {
        chapter.setTextbookId(request.getTextbookId());
        chapter.setName(request.getName());
        chapter.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        chapter.setChapterOrder(request.getChapterOrder() == null ? 0 : request.getChapterOrder());
        chapter.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }
}
