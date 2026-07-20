package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.dal.education.bo.LearningStateBO;
import com.shiyu.ai.education.domain.LearningState;
import com.shiyu.ai.dal.education.repository.LearningStateRepository;
import com.shiyu.ai.education.service.LearningStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningStateMachineImpl implements LearningStateMachine {

    private final LearningStateRepository learningStateRepository;

    @Override
    public LearningState getState(Long studentId, Long knowledgeId) {
        LearningStateBO stateDO = learningStateRepository.selectByStudentAndKnowledge(studentId, knowledgeId);
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
    public void startLearning(Long studentId, Long knowledgeId) {
        updateState(studentId, knowledgeId, getState(studentId, knowledgeId).startLearning());
    }

    @Override
    public void passAssessment(Long studentId, Long knowledgeId) {
        updateState(studentId, knowledgeId, getState(studentId, knowledgeId).passAssessment());
    }

    @Override
    public void deepPractice(Long studentId, Long knowledgeId) {
        updateState(studentId, knowledgeId, getState(studentId, knowledgeId).deepPractice());
    }

    @Override
    public void forget(Long studentId, Long knowledgeId) {
        updateState(studentId, knowledgeId, getState(studentId, knowledgeId).forget());
    }

    @Override
    public void scheduleReview(Long studentId, Long knowledgeId) {
        updateState(studentId, knowledgeId, getState(studentId, knowledgeId).scheduleReview());
    }

    @Override
    public void giveUp(Long studentId, Long knowledgeId) {
        updateState(studentId, knowledgeId, getState(studentId, knowledgeId).giveUp());
    }

    private void updateState(Long studentId, Long knowledgeId, LearningState newState) {
        LearningStateBO stateDO = new LearningStateBO();
        stateDO.setStudentId(studentId);
        stateDO.setKnowledgeId(knowledgeId);
        stateDO.setState(newState.name());
        learningStateRepository.upsert(stateDO);
        log.info("学习状态已持久化: student={}, knowledge={}, state={}", studentId, knowledgeId, newState);
    }
}
