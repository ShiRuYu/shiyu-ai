package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.StudyPlanResponse;
import com.shiyu.ai.education.implementation.web.request.StudyPlanRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * StudyPlanService 服务接口，负责执行教育领域相关业务操作。
 */
public interface StudyPlanService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    StudyPlanResponse getById(ActorContext actor, Long id);

    /**
     * 根据学生标识查询学习计划列表。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     *
     * @return 结果列表。
     */
    List<StudyPlanResponse> listByStudentId(ActorContext actor, Long studentId);

    /**
     * 查询启用按学生列表。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     *
     * @return 结果列表。
     */
    List<StudyPlanResponse> listActiveByStudent(ActorContext actor, Long studentId);

    /**
     * 创建学习计划。
     *
     * @param actor 调用方上下文。
     * @param plan 学习计划请求。
     *
     * @return 处理后的学习计划。
     */
    StudyPlanResponse create(ActorContext actor, StudyPlanRequest plan);

    /**
     * 更新学习计划。
     *
     * @param actor 调用方上下文。
     * @param plan 学习计划请求。
     */
    void update(ActorContext actor, StudyPlanRequest plan);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 获取今日tasks。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     *
     * @return 结果列表。
     */
    List<com.shiyu.ai.education.implementation.web.dto.DailyTaskResponse> getTodayTasks(
            ActorContext actor, Long studentId);
}
