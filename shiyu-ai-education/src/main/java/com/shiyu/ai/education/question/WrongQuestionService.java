package com.shiyu.ai.education.question;

import com.shiyu.ai.dal.dataobject.education.WrongQuestionDO;

import java.util.List;

/**
 * Wrong Question 接口
 */

public interface WrongQuestionService {

    /**
     * Get By Id
     * @return 处理结果
     */
    WrongQuestionDO getById(Long id);

    /**
     * List By Student Id
     * @return 处理结果
     */
    List<WrongQuestionDO> listByStudentId(Long studentId);

    /**
     * Get By Student And Question
     * @return 处理结果
     */
    WrongQuestionDO getByStudentAndQuestion(Long studentId, Long questionId);

    /**
     * Create
     * @param WrongQuestionDO WrongQuestionDO
     * @return 处理结果
     */
    WrongQuestionDO create(WrongQuestionDO wrongQuestion);

    /**
     * Update
     * @param WrongQuestionDO WrongQuestionDO
     * @return 处理结果
     */
    void update(WrongQuestionDO wrongQuestion);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
