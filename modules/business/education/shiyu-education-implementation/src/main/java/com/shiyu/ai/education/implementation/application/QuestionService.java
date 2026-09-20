package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.QuestionResponse;
import com.shiyu.ai.education.implementation.web.request.QuestionRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 题目 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface QuestionService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    QuestionResponse getById(ActorContext actor, Long id);

    /**
     * 根据编码获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param code code 参数。
     *
     * @return 处理结果。
     */
    QuestionResponse getByCode(ActorContext actor, String code);

    /**
     * 分页查询题目。
     *
     * @param actor 调用方上下文。
     * @param pageNum 页码。
     * @param pageSize 每页条数。
     *
     * @return 分页查询结果。
     */
    PageData<QuestionResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @param grade 用于完成本次业务处理的 grade 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<QuestionResponse> listBySubjectAndGrade(
            ActorContext actor, String subjectCode, Integer grade);

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param difficulty 用于完成本次业务处理的 difficulty 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<QuestionResponse> listByDifficulty(ActorContext actor, Integer difficulty);

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<QuestionResponse> listByType(ActorContext actor, String type);

    /**
     * 创建题目。
     *
     * @param actor 调用方上下文。
     * @param question 题目请求。
     *
     * @return 处理后的题目。
     */
    QuestionResponse create(ActorContext actor, QuestionRequest question);

    /**
     * 更新题目。
     *
     * @param actor 调用方上下文。
     * @param question 题目请求。
     */
    void update(ActorContext actor, QuestionRequest question);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 处理incrementusedcount。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void incrementUsedCount(ActorContext actor, Long id);
}
