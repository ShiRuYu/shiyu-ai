package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.education.bo.StudyPlanBO;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.StudyPlanResponse;
import com.shiyu.ai.education.request.StudyPlanRequest;

/**
 * Study Plan 接口
 */

public interface StudyPlanService {

    /**
     * Get By Id
     * @return 处理结果
     */
    StudyPlanResponse getById(Long id);

    /**
     * List By Student Id
     * @return 处理结果
     */
    List<StudyPlanResponse> listByStudentId(Long studentId);

    /**
     * List Active By Student
     * @return 处理结果
     */
    List<StudyPlanResponse> listActiveByStudent(Long studentId);

    /**
     * Create
     * @param StudyPlanResponse StudyPlanDO
     * @return 处理结果
     */
    StudyPlanResponse create(StudyPlanRequest plan);

    /**
     * Update
     * @param StudyPlanResponse StudyPlanDO
     * @return 处理结果
     */
    void update(StudyPlanRequest plan);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);

    /**
     * Get Today Tasks
     */
    List<com.shiyu.ai.education.dto.DailyTaskResponse> getTodayTasks(Long studentId);
}
