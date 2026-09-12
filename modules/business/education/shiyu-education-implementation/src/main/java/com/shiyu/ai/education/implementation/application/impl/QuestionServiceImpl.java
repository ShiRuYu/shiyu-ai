package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.QuestionService;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.domain.port.repository.QuestionRepository;
import com.shiyu.ai.education.implementation.web.dto.QuestionResponse;
import com.shiyu.ai.education.implementation.web.request.QuestionRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code QuestionServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    /**
     * 题目仓储，表示当前对象中的对应属性。
     */
    private final QuestionRepository questionRepository;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public QuestionResponse getById(ActorContext actor, Long id) {
        QuestionBO bo = questionRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    /**
     * {@code getByCode} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public QuestionResponse getByCode(ActorContext actor, String code) {
        QuestionBO bo = questionRepository.selectByCode(actor.tenantId(), code);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    /**
     * {@code listBySubjectAndGrade} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param subjectCode 参数值，用于执行当前操作。
     * @param grade 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<QuestionResponse> listBySubjectAndGrade(
            ActorContext actor, String subjectCode, Integer grade) {
        List<QuestionBO> boList =
                questionRepository.selectBySubjectAndGrade(actor.tenantId(), subjectCode, grade);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    /**
     * {@code listByDifficulty} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param difficulty 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<QuestionResponse> listByDifficulty(ActorContext actor, Integer difficulty) {
        List<QuestionBO> boList =
                questionRepository.selectByDifficulty(actor.tenantId(), difficulty);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    /**
     * {@code listByType} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<QuestionResponse> listByType(ActorContext actor, String type) {
        List<QuestionBO> boList = questionRepository.selectByType(actor.tenantId(), type);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PageData<QuestionResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<QuestionBO> boPage =
                questionRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<QuestionResponse> items =
                MapstructUtils.convert(boPage.getItems(), QuestionResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     */
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

    /**
     * {@code incrementUsedCount} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void incrementUsedCount(ActorContext actor, Long id) {
        questionRepository.incrementUsedCount(actor.tenantId(), id);
    }

    /**
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        questionRepository.deleteById(actor.tenantId(), id);
    }
}
