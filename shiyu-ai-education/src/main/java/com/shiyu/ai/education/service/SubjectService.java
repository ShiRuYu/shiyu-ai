package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.bo.education.SubjectBO;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.SubjectResponse;
import com.shiyu.ai.education.request.SubjectRequest;

/**
 * Subject 接口
 */

public interface SubjectService {

    /**
     * Get By Id
     * @return 处理结果
     */
    SubjectResponse getById(Long id);

    /**
     * Get By Code
     * @return 处理结果
     */
    SubjectResponse getByCode(String code);

    /**
     * List All
     * @return 处理结果
     */

    PageData<SubjectResponse> page(int pageNum, int pageSize);

    /**
     * List By Grade Level
     * @return 处理结果
     */
    List<SubjectResponse> listByGradeLevel(String gradeLevel);

    /**
     * Create
     * @param SubjectResponse SubjectDO
     * @return 处理结果
     */
    SubjectResponse create(SubjectRequest subject);

    /**
     * Update
     * @param SubjectResponse SubjectDO
     * @return 处理结果
     */
    void update(SubjectRequest subject);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
