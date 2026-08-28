package com.shiyu.ai.education.service;

import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.request.ReviewRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * Review 接口
 */

public interface ReviewService {

    /**
     * Get By Id
     * @return 处理结果
     */
    ReviewTaskResponse getById(ActorContext actor, Long id);

    /**
     * List Today Tasks
     * @return 处理结果
     */
    List<ReviewTaskResponse> listTodayTasks(ActorContext actor, Long studentId);

    /**
     * List By Student And Status
     * @return 处理结果
     */
    List<ReviewTaskResponse> listByStudentAndStatus(ActorContext actor, Long studentId, Integer status);

    /**
     * List By Student And Knowledge
     * @return 处理结果
     */
    List<ReviewTaskResponse> listByStudentAndKnowledge(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * Create
     * @param ReviewRequest ReviewRequest
     * @return 处理结果
     */
    ReviewTaskResponse create(ActorContext actor, ReviewRequest request);

    /**
     * Update
     * @param ReviewRequest ReviewRequest
     * @return 处理结果
     */
    void update(ActorContext actor, ReviewRequest request);

    void complete(ActorContext actor, Long id, Double resultScore);

    void delete(ActorContext actor, Long id);
}
