package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.ExamResponse;
import com.shiyu.ai.education.request.ExamRequest;

/**
 * Exam 接口
 */

public interface ExamService {

    /**
     * List All
     */

    PageData<ExamResponse> page(int pageNum, int pageSize);

    /**
     * Get By Id
     * @return 处理结果
     */
    ExamResponse getById(Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<ExamResponse> listBySubjectCode(String subjectCode);

    /**
     * List By Teacher Id
     * @return 处理结果
     */
    List<ExamResponse> listByTeacherId(Long teacherId);

    /**
     * Create
     * @param ExamResponse ExamDO
     * @return 处理结果
     */
    ExamResponse create(ExamRequest exam);

    /**
     * Update
     * @param ExamResponse ExamDO
     * @return 处理结果
     */
    void update(ExamRequest exam);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);

    /**
     * Submit exam answer
     */
    ExamResponse submit(Long id, com.shiyu.ai.education.dto.SubmitAnswerRequest request);
}
