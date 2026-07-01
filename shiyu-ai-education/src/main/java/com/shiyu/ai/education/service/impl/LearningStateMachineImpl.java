package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.LearningState;
import com.shiyu.ai.education.service.LearningStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LearningStateMachineImpl implements LearningStateMachine {

    private final Map<String, LearningState> states = new ConcurrentHashMap<>();

    private String key(Long studentId, Long knowledgeId) {
        return studentId + ":" + knowledgeId;
    }

    @Override
    public LearningState getState(Long studentId, Long knowledgeId) {
        return states.getOrDefault(key(studentId, knowledgeId), LearningState.NOT_STARTED);
    }

    @Override
    public void startLearning(Long studentId, Long knowledgeId) {
        setState(studentId, knowledgeId, getState(studentId, knowledgeId).startLearning());
    }

    @Override
    public void passAssessment(Long studentId, Long knowledgeId) {
        setState(studentId, knowledgeId, getState(studentId, knowledgeId).passAssessment());
    }

    @Override
    public void deepPractice(Long studentId, Long knowledgeId) {
        setState(studentId, knowledgeId, getState(studentId, knowledgeId).deepPractice());
    }

    @Override
    public void forget(Long studentId, Long knowledgeId) {
        setState(studentId, knowledgeId, getState(studentId, knowledgeId).forget());
    }

    @Override
    public void scheduleReview(Long studentId, Long knowledgeId) {
        setState(studentId, knowledgeId, getState(studentId, knowledgeId).scheduleReview());
    }

    @Override
    public void giveUp(Long studentId, Long knowledgeId) {
        setState(studentId, knowledgeId, getState(studentId, knowledgeId).giveUp());
    }

    private void setState(Long studentId, Long knowledgeId, LearningState newState) {
        states.put(key(studentId, knowledgeId), newState);
        log.debug("学习状态变更: student={}, knowledge={}, newState={}", studentId, knowledgeId, newState);
    }
}
