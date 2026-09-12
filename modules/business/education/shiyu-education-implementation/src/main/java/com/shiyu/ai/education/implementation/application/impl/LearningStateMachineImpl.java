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
 * {@code LearningStateMachineImpl} 承载教育模块的领域状态或协作行为，负责维护本类型的职责边界。
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
     * {@code getState} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code startLearning} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
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
     * {@code passAssessment} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
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
     * {@code deepPractice} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
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
     * {@code forget} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     */
    @Override
    public void forget(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(
                actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).forget());
    }

    /**
     * {@code scheduleReview} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
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
     * {@code giveUp} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
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
