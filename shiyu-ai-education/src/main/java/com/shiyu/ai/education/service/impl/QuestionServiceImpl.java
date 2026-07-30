package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.QuestionBO;
import com.shiyu.ai.dal.education.repository.QuestionRepository;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.request.QuestionRequest;
import com.shiyu.ai.education.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    public QuestionResponse getById(Long id) {
        QuestionBO bo = questionRepository.selectById(id);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    @Override
    public QuestionResponse getByCode(String code) {
        QuestionBO bo = questionRepository.selectByCode(code);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    @Override
    public List<QuestionResponse> listBySubjectAndGrade(String subjectCode, Integer grade) {
        List<QuestionBO> boList = questionRepository.selectBySubjectAndGrade(subjectCode, grade);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    @Override
    public List<QuestionResponse> listByDifficulty(Integer difficulty) {
        List<QuestionBO> boList = questionRepository.selectByDifficulty(difficulty);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    @Override
    public List<QuestionResponse> listByType(String type) {
        List<QuestionBO> boList = questionRepository.selectByType(type);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    @Override
    public PageData<QuestionResponse> page(int pageNum, int pageSize) {
        PageData<QuestionBO> boPage = questionRepository.selectPage(pageNum, pageSize);
        List<QuestionResponse> items = MapstructUtils.convert(boPage.getItems(), QuestionResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse create(QuestionRequest request) {
        QuestionBO bo = new QuestionBO();
        bo.setCode(request.getCode());
        bo.setType(request.getType());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setDifficulty(request.getDifficulty());
        bo.setAbilityDimension(request.getAbilityDimension());
        bo.setTitle(request.getTitle());
        bo.setOptions(request.getOptions());
        bo.setAnswer(request.getAnswer());
        bo.setAnalysis(request.getAnalysis());
        bo.setTags(request.getTags());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        questionRepository.insert(bo);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(QuestionRequest request) {
        QuestionBO bo = questionRepository.selectById(request.getId());
        if (bo != null) {
            bo.setCode(request.getCode());
            bo.setType(request.getType());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setDifficulty(request.getDifficulty());
            bo.setAbilityDimension(request.getAbilityDimension());
            bo.setTitle(request.getTitle());
            bo.setOptions(request.getOptions());
            bo.setAnswer(request.getAnswer());
            bo.setAnalysis(request.getAnalysis());
            bo.setTags(request.getTags());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            questionRepository.update(bo);
        }
    }

    @Override
    public void incrementUsedCount(Long id) {
        questionRepository.incrementUsedCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }
}
