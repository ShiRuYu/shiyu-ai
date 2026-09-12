package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.request.ExamRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * ExamService 服务接口，负责执行教育领域相关业务操作。
 */
public interface ExamService {

    /**
     * 分页查询exam。
     *
     * @param actor 调用方上下文。
     * @param pageNum 页码。
     * @param pageSize 每页条数。
     *
     * @return 分页查询结果。
     */
    PageData<ExamResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    ExamResponse getById(ActorContext actor, Long id);

    /**
     * 根据学科编码查询考试列表。
     *
     * @param actor 调用方上下文。
     * @param subjectCode 学科编码。
     *
     * @return 结果列表。
     */
    List<ExamResponse> listBySubjectCode(ActorContext actor, String subjectCode);

    /**
     * 根据教师标识查询考试列表。
     *
     * @param actor 调用方上下文。
     * @param teacherId teacherId 参数。
     *
     * @return 结果列表。
     */
    List<ExamResponse> listByTeacherId(ActorContext actor, Long teacherId);

    /**
     * 创建考试。
     *
     * @param actor 调用方上下文。
     * @param exam 考试请求。
     *
     * @return 处理后的考试。
     */
    ExamResponse create(ActorContext actor, ExamRequest exam);

    /**
     * 更新考试。
     *
     * @param actor 调用方上下文。
     * @param exam 考试请求。
     */
    void update(ActorContext actor, ExamRequest exam);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 处理submit。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     * @param request 请求对象。
     *
     * @return 处理结果。
     */
    ExamResponse submit(
            ActorContext actor,
            Long id,
            com.shiyu.ai.education.implementation.web.dto.SubmitAnswerRequest request);
}
