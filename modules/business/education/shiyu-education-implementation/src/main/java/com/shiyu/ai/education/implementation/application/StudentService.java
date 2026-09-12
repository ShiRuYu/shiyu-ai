package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.StudentResponse;
import com.shiyu.ai.education.implementation.web.request.StudentRequest;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * StudentService 服务接口，负责执行教育领域相关业务操作。
 */
public interface StudentService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    StudentResponse getById(ActorContext actor, Long id);

    /**
     * 根据用户标识获取学生记录。
     *
     * @param actor 调用方上下文。
     * @param userId 用户标识。
     *
     * @return 处理结果。
     */
    StudentResponse getByUserId(ActorContext actor, Long userId);

    /**
     * 创建学生。
     *
     * @param actor 调用方上下文。
     * @param student 学生请求。
     *
     * @return 处理后的学生。
     */
    StudentResponse create(ActorContext actor, StudentRequest student);

    /**
     * 分页查询学生。
     *
     * @param actor 调用方上下文。
     * @param pageNum 页码。
     * @param pageSize 每页条数。
     *
     * @return 分页查询结果。
     */
    PageData<StudentResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * 更新学生。
     *
     * @param actor 调用方上下文。
     * @param student 学生请求。
     */
    void update(ActorContext actor, StudentRequest student);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);
}
