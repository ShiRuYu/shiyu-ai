package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.dal.dataobject.education.ChapterDO;
import com.shiyu.ai.dal.dataobject.education.KnowledgeTextbookDO;
import com.shiyu.ai.education.service.ChapterService;
import com.shiyu.ai.dal.repository.education.ChapterRepository;
import com.shiyu.ai.dal.repository.education.KnowledgeTextbookRepository;
import jakarta.annotation.Resource;
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

    @Resource
    private KnowledgeTextbookRepository knowledgeTextbookRepository;

    @Override
    public ChapterDO getById(Long id) {
        return chapterRepository.selectById(id);
    }

    @Override
    public List<ChapterDO> listByTextbookId(Long textbookId) {
        return chapterRepository.selectByTextbookId(textbookId);
    }

    @Override
    public List<ChapterDO> listRootChapters(Long textbookId) {
        return chapterRepository.selectRootChapters(textbookId);
    }

    @Override
    public List<ChapterDO> listByParentId(Long parentId) {
        return chapterRepository.selectByParentId(parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterDO create(ChapterDO chapter) {
        chapterRepository.insert(chapter);
        return chapter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ChapterDO chapter) {
        chapterRepository.update(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        chapterRepository.deleteById(id);
    }

    @Override
    public List<Long> getKnowledgeIdsByChapterId(Long chapterId) {
        return knowledgeTextbookRepository.selectByChapterId(chapterId)
                .stream()
                .map(KnowledgeTextbookDO::getKnowledgeId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindKnowledge(Long chapterId, List<Long> knowledgeIds) {
        ChapterDO chapter = chapterRepository.selectById(chapterId);
        if (chapter == null) return;

        for (Long knowledgeId : knowledgeIds) {
            KnowledgeTextbookDO kt = new KnowledgeTextbookDO();
            kt.setKnowledgeId(knowledgeId);
            kt.setTextbookId(chapter.getTextbookId());
            kt.setChapterId(chapterId);
            knowledgeTextbookRepository.insert(kt);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindKnowledge(Long chapterId, Long knowledgeId) {
        knowledgeTextbookRepository.deleteByKnowledgeIdAndChapterId(knowledgeId, chapterId);
    }
}