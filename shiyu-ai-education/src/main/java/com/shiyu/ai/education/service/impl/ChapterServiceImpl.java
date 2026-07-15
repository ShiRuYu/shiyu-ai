package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.ChapterBO;
import com.shiyu.ai.dal.repository.education.ChapterRepository;
import com.shiyu.ai.dal.repository.education.KnowledgeTextbookRepository;
import com.shiyu.ai.education.dto.ChapterResponse;
import com.shiyu.ai.education.service.ChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
