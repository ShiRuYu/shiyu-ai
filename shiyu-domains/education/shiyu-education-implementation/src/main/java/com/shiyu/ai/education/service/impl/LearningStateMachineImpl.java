package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.model.LearningStateBO;
import com.shiyu.ai.education.domain.LearningState;
import com.shiyu.ai.education.port.repository.LearningStateRepository;
import com.shiyu.ai.education.service.LearningStateMachine;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningStateMachineImpl implements LearningStateMachine {

    private final LearningStateRepository learningStateRepository;

    @Override
    public LearningState getState(ActorContext actor, Long studentId, Long knowledgeId) {
        LearningStateBO stateDO = learningStateRepository.selectByStudentAndKnowledge(actor.tenantId(), studentId, knowledgeId);
        if (stateDO == null) {
            return LearningState.NOT_STARTED;
        }
        try {
            return LearningState.valueOf(stateDO.getState());
        } catch (IllegalArgumentException e) {
            return LearningState.NOT_STARTED;
        }
    }

    @Override
    public void startLearning(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).startLearning());
    }

    @Override
    public void passAssessment(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).passAssessment());
    }

    @Override
    public void deepPractice(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).deepPractice());
    }

    @Override
    public void forget(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).forget());
    }

    @Override
    public void scheduleReview(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).scheduleReview());
    }

    @Override
    public void giveUp(ActorContext actor, Long studentId, Long knowledgeId) {
        updateState(actor, studentId, knowledgeId, getState(actor, studentId, knowledgeId).giveUp());
    }

    private void updateState(ActorContext actor, Long studentId, Long knowledgeId, LearningState newState) {
        LearningStateBO stateDO = new LearningStateBO();
        stateDO.setStudentId(studentId);
        stateDO.setKnowledgeId(knowledgeId);
        stateDO.setState(newState.name());
        learningStateRepository.upsert(actor.tenantId(), stateDO);
        log.info("学习状态已持久化: student={}, knowledge={}, state={}", studentId, knowledgeId, newState);
    }
}
