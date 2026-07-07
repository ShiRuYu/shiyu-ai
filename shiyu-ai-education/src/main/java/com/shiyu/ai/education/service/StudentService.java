package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.dataobject.education.StudentDO;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

/**
 * Student 接口
 */

public interface StudentService {

    /**
     * Get By Id
     * @return 处理结果
     */
    StudentDO getById(Long id);

    /**
     * Get By User Id
     * @return 处理结果
     */
    StudentDO getByUserId(Long userId);

    /**
     * Create
     * @param StudentDO StudentDO
     * @return 处理结果
     */
    StudentDO create(StudentDO student);

    /**
     * List All
     */
    List<StudentDO> listAll();

    PageData<StudentDO> page(int pageNum, int pageSize);

    /**
     * Update
     * @param StudentDO StudentDO
     * @return 处理结果
     */
    void update(StudentDO student);

    /**
     * Delete By Id
     */
    void deleteById(Long id);
}
