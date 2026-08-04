package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.TextbookResponse;
import com.shiyu.ai.education.request.TextbookRequest;

/**
 * Textbook 接口
 */

public interface TextbookService {

    /**
     * Get By Id
     * @return 处理结果
     */
    TextbookResponse getById(Long id);

    /**
     * List By Subject And Grade
     * @return 处理结果
     */
    List<TextbookResponse> listBySubjectAndGrade(String subjectCode, Integer grade);

    /**
     * List All
     * @return 处理结果
     */

    PageData<TextbookResponse> page(int pageNum, int pageSize);

    /**
     * Create
     * @param TextbookResponse TextbookDO
     * @return 处理结果
     */
    TextbookResponse create(TextbookRequest textbook);

    /**
     * Update
     * @param TextbookResponse TextbookDO
     * @return 处理结果
     */
    void update(TextbookRequest textbook);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
