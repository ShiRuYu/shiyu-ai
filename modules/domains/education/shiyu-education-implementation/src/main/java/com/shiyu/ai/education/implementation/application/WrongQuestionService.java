package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.WrongQuestionResponse;
import com.shiyu.ai.education.implementation.web.request.WrongQuestionRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/** Wrong Question 接口 */
public interface WrongQuestionService {

    /**
     * Get By Id
     *
     * @return 处理结果
     */
    WrongQuestionResponse getById(ActorContext actor, Long id);

    /**
     * List By Student Id
     *
     * @return 处理结果
     */
    List<WrongQuestionResponse> listByStudentId(ActorContext actor, Long studentId);

    /**
     * Get By Student And Question
     *
     * @return 处理结果
     */
    WrongQuestionResponse getByStudentAndQuestion(
            ActorContext actor, Long studentId, Long questionId);

    /**
     * Create
     *
     * @param WrongQuestionResponse WrongQuestionDO
     * @return 处理结果
     */
    WrongQuestionResponse create(ActorContext actor, WrongQuestionRequest wrongQuestion);

    /**
     * Update
     *
     * @param WrongQuestionResponse WrongQuestionDO
     * @return 处理结果
     */
    void update(ActorContext actor, WrongQuestionRequest wrongQuestion);

    /**
     * Delete By Id
     *
     * @return 处理结果
     */
    void deleteById(ActorContext actor, Long id);
}
