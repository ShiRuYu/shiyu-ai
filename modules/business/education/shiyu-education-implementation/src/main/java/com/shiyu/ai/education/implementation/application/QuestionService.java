package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.QuestionResponse;
import com.shiyu.ai.education.implementation.web.request.QuestionRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * QuestionService 服务接口，负责执行教育领域相关业务操作。
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
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param subjectCode 方法参数。
     * @param grade 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<QuestionResponse> listBySubjectAndGrade(
            ActorContext actor, String subjectCode, Integer grade);

    /**
     * 根据难度查询题目列表。
     *
     * @param actor 调用方上下文。
     * @param difficulty difficulty 参数。
     *
     * @return 结果列表。
     */
    List<QuestionResponse> listByDifficulty(ActorContext actor, Integer difficulty);

    /**
     * 根据类型查询题目列表。
     *
     * @param actor 调用方上下文。
     * @param type 数据类型。
     *
     * @return 结果列表。
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
