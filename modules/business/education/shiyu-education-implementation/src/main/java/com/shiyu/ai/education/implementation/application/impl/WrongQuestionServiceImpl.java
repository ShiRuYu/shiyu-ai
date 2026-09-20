package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.WrongQuestionService;
import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;
import com.shiyu.ai.education.implementation.domain.port.repository.WrongQuestionRepository;
import com.shiyu.ai.education.implementation.web.dto.WrongQuestionResponse;
import com.shiyu.ai.education.implementation.web.request.WrongQuestionRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 提供 Wrong 题目 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    /**
     * wrongQuestionRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final WrongQuestionRepository wrongQuestionRepository;

    /**
     * 查询 Wrong 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Wrong 题目 相关操作生成的结果数据。
     */
    @Override
    public WrongQuestionResponse getById(ActorContext actor, Long id) {
        WrongQuestionBO bo = wrongQuestionRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    /**
     * 查询 Wrong 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<WrongQuestionResponse> listByStudentId(ActorContext actor, Long studentId) {
        List<WrongQuestionBO> boList =
                wrongQuestionRepository.selectByStudentId(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, WrongQuestionResponse.class);
    }

    /**
     * 查询 Wrong 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param questionId 用于定位question的标识。
     * @return 返回 Wrong 题目 相关操作生成的结果数据。
     */
    @Override
    public WrongQuestionResponse getByStudentAndQuestion(
            ActorContext actor, Long studentId, Long questionId) {
        WrongQuestionBO bo =
                wrongQuestionRepository.selectByStudentAndQuestion(
                        actor.tenantId(), studentId, questionId);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    /**
     * 执行 Wrong 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WrongQuestionResponse create(ActorContext actor, WrongQuestionRequest request) {
        WrongQuestionBO bo = new WrongQuestionBO();
        bo.setStudentId(request.getStudentId());
        bo.setQuestionId(request.getQuestionId());
        bo.setKnowledgeId(request.getKnowledgeId());
        bo.setStudentAnswer(request.getStudentAnswer());
        bo.setCorrectTimes(request.getCorrectTimes() == null ? 0 : request.getCorrectTimes());
        bo.setStatus(request.getStatus() == null ? 0 : request.getStatus());
        wrongQuestionRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    /**
     * 执行 Wrong 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, WrongQuestionRequest request) {
        WrongQuestionBO bo = wrongQuestionRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setStudentId(request.getStudentId());
            bo.setQuestionId(request.getQuestionId());
            bo.setKnowledgeId(request.getKnowledgeId());
            bo.setStudentAnswer(request.getStudentAnswer());
            bo.setCorrectTimes(request.getCorrectTimes());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            wrongQuestionRepository.update(actor.tenantId(), bo);
        }
    }

    /**
     * 执行 Wrong 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        wrongQuestionRepository.deleteById(actor.tenantId(), id);
    }
}
