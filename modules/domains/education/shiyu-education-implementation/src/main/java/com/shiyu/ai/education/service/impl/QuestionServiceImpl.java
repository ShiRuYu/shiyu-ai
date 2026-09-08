package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.QuestionBO;
import com.shiyu.ai.education.port.repository.QuestionRepository;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.request.QuestionRequest;
import com.shiyu.ai.education.service.QuestionService;
import com.shiyu.ai.kernel.context.ActorContext;
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
    public QuestionResponse getById(ActorContext actor, Long id) {
        QuestionBO bo = questionRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    @Override
    public QuestionResponse getByCode(ActorContext actor, String code) {
        QuestionBO bo = questionRepository.selectByCode(actor.tenantId(), code);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    @Override
    public List<QuestionResponse> listBySubjectAndGrade(ActorContext actor, String subjectCode, Integer grade) {
        List<QuestionBO> boList = questionRepository.selectBySubjectAndGrade(actor.tenantId(), subjectCode, grade);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    @Override
    public List<QuestionResponse> listByDifficulty(ActorContext actor, Integer difficulty) {
        List<QuestionBO> boList = questionRepository.selectByDifficulty(actor.tenantId(), difficulty);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    @Override
    public List<QuestionResponse> listByType(ActorContext actor, String type) {
        List<QuestionBO> boList = questionRepository.selectByType(actor.tenantId(), type);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    @Override
    public PageData<QuestionResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<QuestionBO> boPage = questionRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<QuestionResponse> items = MapstructUtils.convert(boPage.getItems(), QuestionResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse create(ActorContext actor, QuestionRequest request) {
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
        questionRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, QuestionRequest request) {
        QuestionBO bo = questionRepository.selectById(actor.tenantId(), request.getId());
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
            questionRepository.update(actor.tenantId(), bo);
        }
    }

    @Override
    public void incrementUsedCount(ActorContext actor, Long id) {
        questionRepository.incrementUsedCount(actor.tenantId(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        questionRepository.deleteById(actor.tenantId(), id);
    }
}
