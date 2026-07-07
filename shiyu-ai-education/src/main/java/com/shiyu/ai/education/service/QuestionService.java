package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.dataobject.education.QuestionDO;

import java.util.List;

/**
 * Question 接口
 */

public interface QuestionService {

    /**
     * Get By Id
     * @return 处理结果
     */
    QuestionDO getById(Long id);

    /**
     * Get By Code
     * @return 处理结果
     */
    QuestionDO getByCode(String code);

    /**
     * List By Subject And Grade
     * @return 处理结果
     */
    List<QuestionDO> listBySubjectAndGrade(String subjectCode, Integer grade);

    /**
     * List By Difficulty
     * @return 处理结果
     */
    List<QuestionDO> listByDifficulty(Integer difficulty);

    /**
     * List By Type
     * @return 处理结果
     */
    List<QuestionDO> listByType(String type);

    /**
     * Create
     * @param QuestionDO QuestionDO
     * @return 处理结果
     */
    QuestionDO create(QuestionDO question);

    /**
     * Update
     * @param QuestionDO QuestionDO
     * @return 处理结果
     */
    void update(QuestionDO question);

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
