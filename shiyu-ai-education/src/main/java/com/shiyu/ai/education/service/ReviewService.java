package com.shiyu.ai.education.service;

import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.request.ReviewRequest;

import java.util.List;

/**
 * Review 接口
 */

public interface ReviewService {

    /**
     * Get By Id
     * @return 处理结果
     */
    ReviewTaskResponse getById(Long id);

    /**
     * List Today Tasks
     * @return 处理结果
     */
    List<ReviewTaskResponse> listTodayTasks(Long studentId);

    /**
     * List By Student And Status
     * @return 处理结果
     */
    List<ReviewTaskResponse> listByStudentAndStatus(Long studentId, String status);

    /**
     * List By Student And Knowledge
     * @return 处理结果
     */
    List<ReviewTaskResponse> listByStudentAndKnowledge(Long studentId, Long knowledgeId);

    /**
     * Create
     * @param ReviewRequest ReviewRequest
     * @return 处理结果
     */
    ReviewTaskResponse create(ReviewRequest request);

    /**
     * Update
     * @param ReviewRequest ReviewRequest
     * @return 处理结果
     */
    void update(ReviewRequest request);
}
