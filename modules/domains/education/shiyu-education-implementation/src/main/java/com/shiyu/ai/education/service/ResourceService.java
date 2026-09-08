package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.ResourceResponse;
import com.shiyu.ai.education.request.ResourceRequest;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Resource 接口
 */

public interface ResourceService {

    /**
     * Get By Id
     * @return 处理结果
     */
    ResourceResponse getById(ActorContext actor, Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<ResourceResponse> listBySubjectCode(ActorContext actor, String subjectCode);

    /**
     * List By Type
     * @return 处理结果
     */
    List<ResourceResponse> listByType(ActorContext actor, String type);

    /**
     * List All
     * @return 处理结果
     */

    PageData<ResourceResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * Create
     * @param ResourceResponse ResourceDO
     * @return 处理结果
     */
    ResourceResponse create(ActorContext actor, ResourceRequest resource);

    /**
     * Update
     * @param ResourceResponse ResourceDO
     * @return 处理结果
     */
    void update(ActorContext actor, ResourceRequest resource);

    /**
     * Delete By Id
     * @return 处理结果
     */
    List<ResourceResponse> listAll(ActorContext actor);
    void deleteById(ActorContext actor, Long id);
}
