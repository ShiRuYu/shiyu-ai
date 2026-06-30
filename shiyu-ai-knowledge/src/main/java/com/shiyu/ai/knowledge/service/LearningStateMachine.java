package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.knowledge.domain.LearningState;

/**
 * 学习状态机服务
 */
public interface LearningStateMachine {

    LearningState getState(Long studentId, Long knowledgeId);

    void startLearning(Long studentId, Long knowledgeId);

    void passAssessment(Long studentId, Long knowledgeId);

    void deepPractice(Long studentId, Long knowledgeId);

    void forget(Long studentId, Long knowledgeId);

    void scheduleReview(Long studentId, Long knowledgeId);

    void giveUp(Long studentId, Long knowledgeId);
}
