package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.domain.LearningState;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * 定义 Learning State Machine 相关的协作契约和调用边界。
 */
public interface LearningStateMachine {

    /**
     * 查询 Learning State Machine 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回 Learning State Machine 相关操作生成的结果数据。
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
     * 执行 Learning State Machine 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    void passAssessment(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 Learning State Machine 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    void deepPractice(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 Learning State Machine 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    void forget(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 Learning State Machine 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    void scheduleReview(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 执行 Learning State Machine 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     */
    void giveUp(ActorContext actor, Long studentId, Long knowledgeId);
}
