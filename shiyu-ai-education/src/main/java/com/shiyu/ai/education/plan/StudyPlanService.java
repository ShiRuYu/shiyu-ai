package com.shiyu.ai.education.plan;

import com.shiyu.ai.dal.dataobject.education.StudyPlanDO;

import java.util.List;

/**
 * Study Plan 接口
 */

public interface StudyPlanService {

    /**
     * Get By Id
     * @return 处理结果
     */
    StudyPlanDO getById(Long id);

    /**
     * List By Student Id
     * @return 处理结果
     */
    List<StudyPlanDO> listByStudentId(Long studentId);

    /**
     * List Active By Student
     * @return 处理结果
     */
    List<StudyPlanDO> listActiveByStudent(Long studentId);

    /**
     * Create
     * @param StudyPlanDO StudyPlanDO
     * @return 处理结果
     */
    StudyPlanDO create(StudyPlanDO plan);

    /**
     * Update
     * @param StudyPlanDO StudyPlanDO
     * @return 处理结果
     */
    void update(StudyPlanDO plan);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
