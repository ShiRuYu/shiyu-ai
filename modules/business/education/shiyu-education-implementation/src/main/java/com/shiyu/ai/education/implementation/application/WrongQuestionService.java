package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.WrongQuestionResponse;
import com.shiyu.ai.education.implementation.web.request.WrongQuestionRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 Wrong 题目 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 Wrong 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
