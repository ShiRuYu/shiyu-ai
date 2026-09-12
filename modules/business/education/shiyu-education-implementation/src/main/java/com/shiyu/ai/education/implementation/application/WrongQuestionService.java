package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.WrongQuestionResponse;
import com.shiyu.ai.education.implementation.web.request.WrongQuestionRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * WrongQuestionService 服务接口，负责执行教育领域相关业务操作。
 */
public interface WrongQuestionService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    WrongQuestionResponse getById(ActorContext actor, Long id);

    /**
     * 根据学生标识查询错题列表。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     *
     * @return 结果列表。
     */
    List<WrongQuestionResponse> listByStudentId(ActorContext actor, Long studentId);

    /**
     * 根据学生和题目标识获取错题记录。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     * @param questionId questionId 参数。
     *
     * @return 处理结果。
     */
    WrongQuestionResponse getByStudentAndQuestion(
            ActorContext actor, Long studentId, Long questionId);

    /**
     * 创建错题记录。
     *
     * @param actor 调用方上下文。
     * @param wrongQuestion wrongQuestion 参数。
     *
     * @return 处理后的错题记录。
     */
    WrongQuestionResponse create(ActorContext actor, WrongQuestionRequest wrongQuestion);

    /**
     * 更新错题记录。
     *
     * @param actor 调用方上下文。
     * @param wrongQuestion wrongQuestion 参数。
     */
    void update(ActorContext actor, WrongQuestionRequest wrongQuestion);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);
}
