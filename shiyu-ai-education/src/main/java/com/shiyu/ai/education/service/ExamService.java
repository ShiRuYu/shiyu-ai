package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.dataobject.education.ExamDO;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

/**
 * Exam 接口
 */

public interface ExamService {

    /**
     * List All
     */
    List<ExamDO> listAll();

    PageData<ExamDO> page(int pageNum, int pageSize);

    /**
     * Get By Id
     * @return 处理结果
     */
    ExamDO getById(Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<ExamDO> listBySubjectCode(String subjectCode);

    /**
     * List By Teacher Id
     * @return 处理结果
     */
    List<ExamDO> listByTeacherId(Long teacherId);

    /**
     * Create
     * @param ExamDO ExamDO
     * @return 处理结果
     */
    ExamDO create(ExamDO exam);

    /**
     * Update
     * @param ExamDO ExamDO
     * @return 处理结果
     */
    void update(ExamDO exam);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
