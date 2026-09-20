package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.ReviewTaskResponse;
import com.shiyu.ai.education.implementation.web.request.ReviewRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 复习 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 复习 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ReviewTaskResponse> listTodayTasks(ActorContext actor, Long studentId);

    /**
     * 查询 复习 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ReviewTaskResponse> listByStudentAndStatus(
            ActorContext actor, Long studentId, Integer status);

    /**
     * 查询 复习 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param resultScore 用于完成本次业务处理的 resultScore 参数。
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
