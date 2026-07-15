package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.bo.education.QuestionBO;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.request.QuestionRequest;

/**
 * Question 接口
 */

public interface QuestionService {

    /**
     * Get By Id
     * @return 处理结果
     */
    QuestionResponse getById(Long id);

    /**
     * Get By Code
     * @return 处理结果
     */
    QuestionResponse getByCode(String code);

    /**
     * List By Subject And Grade
     * @return 处理结果
     */
    /**
     * List All
     * @return 处理结果
     */

    PageData<QuestionResponse> page(int pageNum, int pageSize);

    List<QuestionResponse> listBySubjectAndGrade(String subjectCode, Integer grade);

    /**
     * List By Difficulty
     * @return 处理结果
     */
    List<QuestionResponse> listByDifficulty(Integer difficulty);

    /**
     * List By Type
     * @return 处理结果
     */
    List<QuestionResponse> listByType(String type);

    /**
     * Create
     * @param QuestionResponse QuestionDO
     * @return 处理结果
     */
    QuestionResponse create(QuestionRequest question);

    /**
     * Update
     * @param QuestionResponse QuestionDO
     * @return 处理结果
     */
    void update(QuestionRequest question);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);

    /**
     * Increment Used Count
     * @return 处理结果
     */
    void incrementUsedCount(Long id);
}
