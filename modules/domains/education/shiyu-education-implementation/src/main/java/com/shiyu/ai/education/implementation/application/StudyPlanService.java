package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.StudyPlanResponse;
import com.shiyu.ai.education.implementation.web.request.StudyPlanRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/** Study Plan 接口 */
public interface StudyPlanService {

    /**
     * Get By Id
     *
     * @return 处理结果
     */
    StudyPlanResponse getById(ActorContext actor, Long id);

    /**
     * List By Student Id
     *
     * @return 处理结果
     */
    List<StudyPlanResponse> listByStudentId(ActorContext actor, Long studentId);

    /**
     * List Active By Student
     *
     * @return 处理结果
     */
    List<StudyPlanResponse> listActiveByStudent(ActorContext actor, Long studentId);

    /**
     * Create
     *
     * @param StudyPlanResponse StudyPlanDO
     * @return 处理结果
     */
    StudyPlanResponse create(ActorContext actor, StudyPlanRequest plan);

    /**
     * Update
     *
     * @param StudyPlanResponse StudyPlanDO
     * @return 处理结果
     */
    void update(ActorContext actor, StudyPlanRequest plan);

    /**
     * Delete By Id
     *
     * @return 处理结果
     */
    void deleteById(ActorContext actor, Long id);

    /** Get Today Tasks */
    List<com.shiyu.ai.education.implementation.web.dto.DailyTaskResponse> getTodayTasks(
            ActorContext actor, Long studentId);
}
