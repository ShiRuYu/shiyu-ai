package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.TextbookResponse;
import com.shiyu.ai.education.implementation.web.request.TextbookRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * TextbookService 服务接口，负责执行教育领域相关业务操作。
 */
public interface TextbookService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    TextbookResponse getById(ActorContext actor, Long id);

    /**
     * 根据学科年级查询教材列表。
     *
     * @param actor 调用方上下文。
     * @param subjectCode 学科编码。
     * @param grade 年级。
     *
     * @return 结果列表。
     */
    List<TextbookResponse> listBySubjectAndGrade(
            ActorContext actor, String subjectCode, Integer grade);

    /**
     * 分页查询教材。
     *
     * @param actor 调用方上下文。
     * @param pageNum 页码。
     * @param pageSize 每页条数。
     *
     * @return 分页查询结果。
     */
    PageData<TextbookResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * 创建教材。
     *
     * @param actor 调用方上下文。
     * @param textbook 教材请求。
     *
     * @return 处理后的教材。
     */
    TextbookResponse create(ActorContext actor, TextbookRequest textbook);

    /**
     * 更新教材。
     *
     * @param actor 调用方上下文。
     * @param textbook 教材请求。
     */
    void update(ActorContext actor, TextbookRequest textbook);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);
}
