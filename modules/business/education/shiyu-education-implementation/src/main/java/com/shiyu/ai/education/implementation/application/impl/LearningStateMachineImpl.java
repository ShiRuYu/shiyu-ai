package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.education.implementation.application.LearningStateMachine;
import com.shiyu.ai.education.implementation.domain.LearningState;
import com.shiyu.ai.education.implementation.domain.model.LearningStateBO;
import com.shiyu.ai.education.implementation.domain.port.repository.LearningStateRepository;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * 编排 Learning State Machine Impl 所属应用流程的输入、协作和业务结果。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningStateMachineImpl implements LearningStateMachine {

    /**
     * learningStateRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final LearningStateRepository learningStateRepository;

    /**
     * 查询 Learning State Machine Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回 Learning State Machine Impl 相关操作生成的结果数据。
     */
    @Override
    public LearningState getState(ActorContext actor, Long studentId, Long knowledgeId) {
        LearningStateBO stateDO =
                learningStateRepository.selectByStudentAndKnowledge(
                        actor.tenantId(), studentId, knowledgeId);
        if (stateDO == null) {
            return LearningState.NOT_STARTED;
        }
        try {
            return LearningState.valueOf(stateDO.getState());
        } catch (IllegalArgumentException e) {
            return LearningState.NOT_STARTED;
        }
    }

    /**
     * 执行 Learning State Machine Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    @Override
    public void startLearning(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(
                actor,
                studentId,
                knowledgeId,
                getState(actor, studentId, knowledgeId).startLearning());
    }

    /**
     * 执行 Learning State Machine Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    @Override
    public void passAssessment(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(
                actor,
                studentId,
                knowledgeId,
                getState(actor, studentId, knowledgeId).passAssessment());
    }

    /**
     * 执行 Learning State Machine Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    @Override
    public void deepPractice(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(
                actor,
                studentId,
                knowledgeId,
                getState(actor, studentId, knowledgeId).deepPractice());
    }

    /**
     * 执行 Learning State Machine Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    @Override
    public void forget(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(
                actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).forget());
    }

    /**
     * 执行 Learning State Machine Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    @Override
    public void scheduleReview(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(
                actor,
                studentId,
                knowledgeId,
                getState(actor, studentId, knowledgeId).scheduleReview());
    }

    /**
     * 执行 Learning State Machine Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    @Override
    public void giveUp(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(
                actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).giveUp());
    }

    private void updateState(
            ActorContext actor, Long studentId, Long knowledgeId, LearningState newState) {
        LearningStateBO stateDO = new LearningStateBO();
        stateDO.setStudentId(studentId);
        stateDO.setKnowledgeId(knowledgeId);
        stateDO.setState(newState.name());
        learningStateRepository.upsert(actor.tenantId(), stateDO);
        log.info(
                "学习状态已持久化: studentIdPresent={}, knowledgeIdPresent={}, state={}",
                studentId != null,
                knowledgeId != null,
                newState);
    }
}
