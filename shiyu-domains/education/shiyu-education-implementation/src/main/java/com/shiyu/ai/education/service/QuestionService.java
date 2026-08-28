package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.request.QuestionRequest;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Question 接口
 */

public interface QuestionService {

    /**
     * Get By Id
     * @return 处理结果
     */
    QuestionResponse getById(ActorContext actor, Long id);

    /**
     * Get By Code
     * @return 处理结果
     */
    QuestionResponse getByCode(ActorContext actor, String code);

    /**
     * List All
     * @return 处理结果
     */

    PageData<QuestionResponse> page(ActorContext actor, int pageNum, int pageSize);

    List<QuestionResponse> listBySubjectAndGrade(ActorContext actor, String subjectCode, Integer grade);

    /**
     * List By Difficulty
     * @return 处理结果
     */
    List<QuestionResponse> listByDifficulty(ActorContext actor, Integer difficulty);

    /**
     * List By Type
     * @return 处理结果
     */
    List<QuestionResponse> listByType(ActorContext actor, String type);

    /**
     * Create
     * @param QuestionResponse QuestionDO
     * @return 处理结果
     */
    QuestionResponse create(ActorContext actor, QuestionRequest question);

    /**
     * Update
     * @param QuestionResponse QuestionDO
     * @return 处理结果
     */
    void update(ActorContext actor, QuestionRequest question);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * Increment Used Count
     * @return 处理结果
     */
    void incrementUsedCount(ActorContext actor, Long id);
}
