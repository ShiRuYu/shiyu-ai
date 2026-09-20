package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.ResourceResponse;
import com.shiyu.ai.education.implementation.web.request.ResourceRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 资源 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface ResourceService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    ResourceResponse getById(ActorContext actor, Long id);

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ResourceResponse> listBySubjectCode(ActorContext actor, String subjectCode);

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ResourceResponse> listByType(ActorContext actor, String type);

    /**
     * 分页查询资源。
     *
     * @param actor 调用方上下文。
     * @param pageNum 页码。
     * @param pageSize 每页条数。
     *
     * @return 分页查询结果。
     */
    PageData<ResourceResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * 创建资源。
     *
     * @param actor 调用方上下文。
     * @param resource 资源请求。
     *
     * @return 处理后的资源。
     */
    ResourceResponse create(ActorContext actor, ResourceRequest resource);

    /**
     * 更新资源。
     *
     * @param actor 调用方上下文。
     * @param resource 资源请求。
     */
    void update(ActorContext actor, ResourceRequest resource);

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ResourceResponse> listAll(ActorContext actor);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);
}
