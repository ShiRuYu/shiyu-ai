package com.shiyu.ai.education.service;

import com.shiyu.ai.education.domain.LearningState;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * 学习状态机服务
 */
public interface LearningStateMachine {

    LearningState getState(ActorContext actor, Long studentId, Long knowledgeId);

    void startLearning(ActorContext actor, Long studentId, Long knowledgeId);

    void passAssessment(ActorContext actor, Long studentId, Long knowledgeId);

    void deepPractice(ActorContext actor, Long studentId, Long knowledgeId);

    void forget(ActorContext actor, Long studentId, Long knowledgeId);

    void scheduleReview(ActorContext actor, Long studentId, Long knowledgeId);

    void giveUp(ActorContext actor, Long studentId, Long knowledgeId);
}
