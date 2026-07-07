package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.dataobject.education.TextbookDO;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

/**
 * Textbook 接口
 */

public interface TextbookService {

    /**
     * Get By Id
     * @return 处理结果
     */
    TextbookDO getById(Long id);

    /**
     * List By Subject And Grade
     * @return 处理结果
     */
    List<TextbookDO> listBySubjectAndGrade(String subjectCode, Integer grade);

    /**
     * List All
     * @return 处理结果
     */
    List<TextbookDO> listAll();

    PageData<TextbookDO> page(int pageNum, int pageSize);

    /**
     * Create
     * @param TextbookDO TextbookDO
     * @return 处理结果
     */
    TextbookDO create(TextbookDO textbook);

    /**
     * Update
     * @param TextbookDO TextbookDO
     * @return 处理结果
     */
    void update(TextbookDO textbook);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
