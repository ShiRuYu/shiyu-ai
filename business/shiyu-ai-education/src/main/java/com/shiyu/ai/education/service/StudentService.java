package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.StudentResponse;
import com.shiyu.ai.education.request.StudentRequest;

/**
 * Student 接口
 */

public interface StudentService {

    /**
     * Get By Id
     * @return 处理结果
     */
    StudentResponse getById(Long id);

    /**
     * Get By User Id
     * @return 处理结果
     */
    StudentResponse getByUserId(Long userId);

    /**
     * Create
     * @param StudentResponse StudentDO
     * @return 处理结果
     */
    StudentResponse create(StudentRequest student);

    /**
     * List All
     */

    PageData<StudentResponse> page(int pageNum, int pageSize);

    /**
     * Update
     * @param StudentResponse StudentDO
     * @return 处理结果
     */
    void update(StudentRequest student);

    /**
     * Delete By Id
     */
    void deleteById(Long id);
}
