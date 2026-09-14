package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.ReviewTaskResponse;
import com.shiyu.ai.education.implementation.web.request.ReviewRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * ReviewService 服务接口，负责执行教育领域相关业务操作。
 */
public interface ReviewService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    ReviewTaskResponse getById(ActorContext actor, Long id);

    /**
     * 查询今日tasks列表。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     *
     * @return 结果列表。
     */
    List<ReviewTaskResponse> listTodayTasks(ActorContext actor, Long studentId);

    /**
     * 根据学生状态查询复习任务列表。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     * @param status 状态。
     *
     * @return 结果列表。
     */
    List<ReviewTaskResponse> listByStudentAndStatus(
            ActorContext actor, Long studentId, Integer status);

    /**
     * 根据学生知识查询复习任务列表。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     * @param knowledgeId knowledgeId 参数。
     *
     * @return 结果列表。
     */
    List<ReviewTaskResponse> listByStudentAndKnowledge(
            ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 创建复习任务。
     *
     * @param actor 调用方上下文。
     * @param request 请求对象。
     *
     * @return 处理后的复习任务。
     */
    ReviewTaskResponse create(ActorContext actor, ReviewRequest request);

    /**
     * 更新复习任务。
     *
     * @param actor 调用方上下文。
     * @param request 请求对象。
     */
    void update(ActorContext actor, ReviewRequest request);

    /**
     * 执行 {@code complete} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param resultScore 方法参数。
     */
    void complete(ActorContext actor, Long id, Double resultScore);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void delete(ActorContext actor, Long id);
}
