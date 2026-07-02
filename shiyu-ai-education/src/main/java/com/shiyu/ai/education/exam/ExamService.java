package com.shiyu.ai.education.exam;

import com.shiyu.ai.dal.dataobject.education.ExamDO;

import java.util.List;

/**
 * Exam 接口
 */

public interface ExamService {

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
