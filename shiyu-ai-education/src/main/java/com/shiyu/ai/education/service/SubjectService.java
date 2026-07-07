package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.dataobject.education.SubjectDO;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

/**
 * Subject 接口
 */

public interface SubjectService {

    /**
     * Get By Id
     * @return 处理结果
     */
    SubjectDO getById(Long id);

    /**
     * Get By Code
     * @return 处理结果
     */
    SubjectDO getByCode(String code);

    /**
     * List All
     * @return 处理结果
     */
    List<SubjectDO> listAll();

    PageData<SubjectDO> page(int pageNum, int pageSize);

    /**
     * List By Grade Level
     * @return 处理结果
     */
    List<SubjectDO> listByGradeLevel(String gradeLevel);

    /**
     * Create
     * @param SubjectDO SubjectDO
     * @return 处理结果
     */
    SubjectDO create(SubjectDO subject);

    /**
     * Update
     * @param SubjectDO SubjectDO
     * @return 处理结果
     */
    void update(SubjectDO subject);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
