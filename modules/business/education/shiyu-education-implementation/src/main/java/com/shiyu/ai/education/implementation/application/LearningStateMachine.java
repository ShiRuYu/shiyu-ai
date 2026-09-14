package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.domain.LearningState;
import com.shiyu.ai.kernel.context.ActorContext;

/** 学习状态机服务 */
public interface LearningStateMachine {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     *
     * @return 操作结果。
     */
    LearningState getState(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 变更当前业务对象的处理状态。
     *
     * @param actor 当前操作主体上下文。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     */
    void startLearning(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 {@code passAssessment} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     */
    void passAssessment(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 {@code deepPractice} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     */
    void deepPractice(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 {@code forget} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     */
    void forget(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 {@code scheduleReview} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     */
    void scheduleReview(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 {@code giveUp} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     */
    void giveUp(ActorContext actor, Long studentId, Long knowledgeId);
}
