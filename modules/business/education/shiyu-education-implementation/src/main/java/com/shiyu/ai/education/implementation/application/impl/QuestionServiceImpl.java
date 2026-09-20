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
 * 提供 题目 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 题目 相关操作生成的结果数据。
     */
    @Override
    public QuestionResponse getById(ActorContext actor, Long id) {
        QuestionBO bo = questionRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 题目 相关操作生成的结果数据。
     */
    @Override
    public QuestionResponse getByCode(ActorContext actor, String code) {
        QuestionBO bo = questionRepository.selectByCode(actor.tenantId(), code);
        return MapstructUtils.convert(bo, QuestionResponse.class);
    }

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @param grade 用于完成本次业务处理的 grade 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<QuestionResponse> listBySubjectAndGrade(
            ActorContext actor, String subjectCode, Integer grade) {
        List<QuestionBO> boList =
                questionRepository.selectBySubjectAndGrade(actor.tenantId(), subjectCode, grade);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param difficulty 用于完成本次业务处理的 difficulty 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<QuestionResponse> listByDifficulty(ActorContext actor, Integer difficulty) {
        List<QuestionBO> boList =
                questionRepository.selectByDifficulty(actor.tenantId(), difficulty);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<QuestionResponse> listByType(ActorContext actor, String type) {
        List<QuestionBO> boList = questionRepository.selectByType(actor.tenantId(), type);
        return MapstructUtils.convert(boList, QuestionResponse.class);
    }

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 题目 相关操作生成的结果数据。
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
     * 执行 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     */
    @Override
    public void incrementUsedCount(ActorContext actor, Long id) {
        questionRepository.incrementUsedCount(actor.tenantId(), id);
    }

    /**
     * 执行 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        questionRepository.deleteById(actor.tenantId(), id);
    }
}
