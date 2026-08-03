package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.education.dto.WrongQuestionResponse;
import com.shiyu.ai.education.request.WrongQuestionRequest;

/**
 * Wrong Question 接口
 */

public interface WrongQuestionService {

    /**
     * Get By Id
     * @return 处理结果
     */
    WrongQuestionResponse getById(Long id);

    /**
     * List By Student Id
     * @return 处理结果
     */
    List<WrongQuestionResponse> listByStudentId(Long studentId);

    /**
     * Get By Student And Question
     * @return 处理结果
     */
    WrongQuestionResponse getByStudentAndQuestion(Long studentId, Long questionId);

    /**
     * Create
     * @param WrongQuestionResponse WrongQuestionDO
     * @return 处理结果
     */
    WrongQuestionResponse create(WrongQuestionRequest wrongQuestion);

    /**
     * Update
     * @param WrongQuestionResponse WrongQuestionDO
     * @return 处理结果
     */
    void update(WrongQuestionRequest wrongQuestion);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
