package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.ResourceResponse;
import com.shiyu.ai.education.request.ResourceRequest;

/**
 * Resource 接口
 */

public interface ResourceService {

    /**
     * Get By Id
     * @return 处理结果
     */
    ResourceResponse getById(Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<ResourceResponse> listBySubjectCode(String subjectCode);

    /**
     * List By Type
     * @return 处理结果
     */
    List<ResourceResponse> listByType(String type);

    /**
     * List All
     * @return 处理结果
     */

    PageData<ResourceResponse> page(int pageNum, int pageSize);

    /**
     * Create
     * @param ResourceResponse ResourceDO
     * @return 处理结果
     */
    ResourceResponse create(ResourceRequest resource);

    /**
     * Update
     * @param ResourceResponse ResourceDO
     * @return 处理结果
     */
    void update(ResourceRequest resource);

    /**
     * Delete By Id
     * @return 处理结果
     */
    List<ResourceResponse> listAll();
    void deleteById(Long id);
}
