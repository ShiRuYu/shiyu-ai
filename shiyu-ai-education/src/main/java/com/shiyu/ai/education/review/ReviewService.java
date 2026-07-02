package com.shiyu.ai.education.review;

import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;

import java.util.List;

/**
 * Review 接口
 */

public interface ReviewService {

    /**
     * Get By Id
     * @return 处理结果
     */
    ReviewTaskDO getById(Long id);

    /**
     * List Today Tasks
     * @return 处理结果
     */
    List<ReviewTaskDO> listTodayTasks(Long studentId);

    /**
     * List By Student And Status
     * @return 处理结果
     */
    List<ReviewTaskDO> listByStudentAndStatus(Long studentId, String status);

    /**
     * List By Student And Knowledge
     * @return 处理结果
     */
    List<ReviewTaskDO> listByStudentAndKnowledge(Long studentId, Long knowledgeId);

    /**
     * Create
     * @param ReviewTaskDO ReviewTaskDO
     * @return 处理结果
     */
    ReviewTaskDO create(ReviewTaskDO task);

    /**
     * Update
     * @param ReviewTaskDO ReviewTaskDO
     * @return 处理结果
     */
    void update(ReviewTaskDO task);
}
