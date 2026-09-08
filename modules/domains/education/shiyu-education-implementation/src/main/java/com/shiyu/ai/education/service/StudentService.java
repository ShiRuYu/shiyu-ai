package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.StudentResponse;
import com.shiyu.ai.education.request.StudentRequest;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Student 接口
 */

public interface StudentService {

    /**
     * Get By Id
     * @return 处理结果
     */
    StudentResponse getById(ActorContext actor, Long id);

    /**
     * Get By User Id
     * @return 处理结果
     */
    StudentResponse getByUserId(ActorContext actor, Long userId);

    /**
     * Create
     * @param StudentResponse StudentDO
     * @return 处理结果
     */
    StudentResponse create(ActorContext actor, StudentRequest student);

    /**
     * List All
     */

    PageData<StudentResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * Update
     * @param StudentResponse StudentDO
     * @return 处理结果
     */
    void update(ActorContext actor, StudentRequest student);

    /**
     * Delete By Id
     */
    void deleteById(ActorContext actor, Long id);
}
