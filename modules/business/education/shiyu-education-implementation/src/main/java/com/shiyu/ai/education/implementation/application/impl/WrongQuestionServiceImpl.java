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
 * {@code WrongQuestionServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
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
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public WrongQuestionResponse getById(ActorContext actor, Long id) {
        WrongQuestionBO bo = wrongQuestionRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    /**
     * {@code listByStudentId} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<WrongQuestionResponse> listByStudentId(ActorContext actor, Long studentId) {
        List<WrongQuestionBO> boList =
                wrongQuestionRepository.selectByStudentId(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, WrongQuestionResponse.class);
    }

    /**
     * {@code getByStudentAndQuestion} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param questionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
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
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        wrongQuestionRepository.deleteById(actor.tenantId(), id);
    }
}
