package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.dataobject.education.ResourceDO;

import java.util.List;

/**
 * Resource 接口
 */

public interface ResourceService {

    /**
     * Get By Id
     * @return 处理结果
     */
    ResourceDO getById(Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<ResourceDO> listBySubjectCode(String subjectCode);

    /**
     * List By Type
     * @return 处理结果
     */
    List<ResourceDO> listByType(String type);

    /**
     * List All
     * @return 处理结果
     */
    List<ResourceDO> listAll();

    /**
     * Create
     * @param ResourceDO ResourceDO
     * @return 处理结果
     */
    ResourceDO create(ResourceDO resource);

    /**
     * Update
     * @param ResourceDO ResourceDO
     * @return 处理结果
     */
    void update(ResourceDO resource);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
