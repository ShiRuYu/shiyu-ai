package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.ResourceResponse;
import com.shiyu.ai.education.implementation.web.request.ResourceRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * ResourceService 服务接口，负责执行教育领域相关业务操作。
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
     * 根据学科编码查询资源列表。
     *
     * @param actor 调用方上下文。
     * @param subjectCode 学科编码。
     *
     * @return 结果列表。
     */
    List<ResourceResponse> listBySubjectCode(ActorContext actor, String subjectCode);

    /**
     * 根据类型查询资源列表。
     *
     * @param actor 调用方上下文。
     * @param type 数据类型。
     *
     * @return 结果列表。
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
     * 查询全部列表。
     *
     * @param actor 调用方上下文。
     *
     * @return 结果列表。
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
