package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.StudyPlanResponse;
import com.shiyu.ai.education.implementation.web.request.StudyPlanRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 Study Plan 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 Study Plan 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<StudyPlanResponse> listByStudentId(ActorContext actor, Long studentId);

    /**
     * 查询 Study Plan 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 Study Plan 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<com.shiyu.ai.education.implementation.web.dto.DailyTaskResponse> getTodayTasks(
            ActorContext actor, Long studentId);
}
